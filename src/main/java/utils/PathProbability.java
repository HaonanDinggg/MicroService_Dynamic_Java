package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 20:51
 **/
public class PathProbability {
    private List<NodeInfo> nodeInfos; //记载当前微服务路径的微服务顺序
    private double probability; //记载当前微服务路径的概率
    private double ArrivalRate; //记载当前微服务路径的到达率 总DAG到达率*probability
    private List<PhysicalRoutingInfo> physicalRoutingInfos;
    private double TighterToleranceLatency; //记载当前微服务路径的收紧容忍时延
    // 路由表存储所有微服务节点的路由转移分布，最终形成三层列表，
    // 第一层：所有微服务
    // 第二层：对应微服务的转移路径 所有有mi节点的转移到所有有mj节点的
    // 第三层：转移路径中对应的具体的每个元素的值 [前继微服务序号，前继节点号，后继微服务序号，后继节点号，转发概率]
    // 链头假设有个虚拟转发器，前继微服务和节点名记为-1
    private List<List<List<Object>>> Routing_tables_eachPath;

    public PathProbability() {
    }


    public PathProbability(List<NodeInfo> nodeInfos, double probability) {
        this.nodeInfos = nodeInfos;
        this.probability = probability;
    }

    public PathProbability(List<NodeInfo> nodeInfos, double probability, double ArrivalRate) {
        this.nodeInfos = nodeInfos;
        this.probability = probability;
        this.ArrivalRate = ArrivalRate;
    }

    public PathProbability(List<NodeInfo> nodeInfos, double probability, double ArrivalRate, List<PhysicalRoutingInfo> physicalRoutingInfos, double TighterToleranceLatency) {
        this.nodeInfos = nodeInfos;
        this.probability = probability;
        this.ArrivalRate = ArrivalRate;
        this.physicalRoutingInfos = physicalRoutingInfos;
        this.TighterToleranceLatency = TighterToleranceLatency;
    }

    /**
     * 更新当前微服务路径路由表,并返回当前微服务路径路由表信息
     * @return Routing_tables_eachPath
     */
    public List<List<List<Object>>> genPathRouting_tables(int[][] InstanceDeployOnNode){
        List<List<List<Object>>> Routing_tables_eachPath = new ArrayList<>();
        //遍历每个微服务
        for (int i = 0; i < nodeInfos.size(); i++) {
            int backward_ms_type = nodeInfos.get(i).getServiceType();
            List<List<Object>> Routing_tables_eachPath_eachMs = new ArrayList<>();
            //如果是入口微服务，则设前继微服务为-1，只需要确定入口节点的概率分布
            if (i == 0){
                //获取该微服务部署节点集合
                List<Integer> nodes = new ArrayList<>();
                int nodes_sum = 0;
                for (int node = 0; node < InstanceDeployOnNode.length; node++){
                    if(InstanceDeployOnNode[node][backward_ms_type] > 0){
                        nodes.add(node);
                        nodes_sum += InstanceDeployOnNode[node][backward_ms_type];
                    }
                }
                //遍历每个节点，计算路由表
                Iterator<Integer> it = nodes.iterator();
                while (it.hasNext()){
                    int backward_ms_node = it.next();
                    List<Object> Routing_table = new ArrayList<>(); //[前继微服务序号，前继节点号，后继微服务序号，后继节点号，转发概率]
                    Routing_table.add(-1); //前继微服务为虚拟转发器
                    Routing_table.add(-1); //前继节点为虚拟转发器
                    Routing_table.add(backward_ms_type); //后继微服务名字
                    Routing_table.add(backward_ms_node); //后继节点名字
                    Routing_table.add((double)InstanceDeployOnNode[backward_ms_node][backward_ms_type] / nodes_sum); //转发概率
                    Routing_tables_eachPath_eachMs.add(Routing_table); //将此路由加入该后继微服务的路由列表
                }
            }else { //非首微服务节点
                int forward_ms_type = nodeInfos.get(i-1).getServiceType();
                //获取该微服务部署节点集合
                List<Integer> forward_nodes = new ArrayList<>();
                List<Integer> backward_nodes = new ArrayList<>();
                int backward_nodes_sum = 0;
                for (int node = 0; node < InstanceDeployOnNode.length; node++){
                    if(InstanceDeployOnNode[node][forward_ms_type] > 0){
                        forward_nodes.add(node);
                    }
                    if(InstanceDeployOnNode[node][backward_ms_type] > 0){
                        backward_nodes.add(node);
                        backward_nodes_sum += InstanceDeployOnNode[node][backward_ms_type];
                    }
                }
                //遍历每个节点，计算路由表
                Iterator<Integer> it_forward = forward_nodes.iterator();
                while (it_forward.hasNext()){
                    int forward_ms_node = it_forward.next();
                    Iterator<Integer> it_backward = backward_nodes.iterator();
                    while (it_backward.hasNext()){
                        int backward_ms_node = it_backward.next();
                        List<Object> Routing_table = new ArrayList<>(); //[前继微服务序号，前继节点号，后继微服务序号，后继节点号，转发概率]
                        Routing_table.add(forward_ms_type); //前继微服务名
                        Routing_table.add(forward_ms_node); //前继节点名
                        Routing_table.add(backward_ms_type); //后继微服务名字
                        Routing_table.add(backward_ms_node); //后继节点名字
                        Routing_table.add((double)InstanceDeployOnNode[backward_ms_node][backward_ms_type] / backward_nodes_sum); //转发概率
                        Routing_tables_eachPath_eachMs.add(Routing_table); //将此路由加入该后继微服务的路由列表
                    }
                }

            }
            Routing_tables_eachPath.add(Routing_tables_eachPath_eachMs);
        }
        this.Routing_tables_eachPath = Routing_tables_eachPath;

        return this.Routing_tables_eachPath;
    }

    /**
     * 获取
     * @return nodeInfos
     */
    public List<NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    /**
     * 设置
     * @param nodeInfos
     */
    public void setNodeInfos(List<NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    /**
     * 获取概率转发调用概率
     * @return probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * 设置概率转发调用概率
     * @param probability
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * 获取
     * @return ArrivalRate
     */
    public double getArrivalRate() {
        return ArrivalRate;
    }

    /**
     * 设置
     * @param ArrivalRate
     */
    public void setArrivalRate(double ArrivalRate) {
        this.ArrivalRate = ArrivalRate;
    }

    public String toString() {
        return "PathProbability{nodeInfos = " + nodeInfos + ", probability = " + probability + ", ArrivalRate = " + ArrivalRate + "}";
    }

    /**
     * 获取
     * @return physicalRoutingInfos
     */
    public List<PhysicalRoutingInfo> getPhysicalRoutingInfos() {
        return physicalRoutingInfos;
    }

    /**
     * 设置
     * @param physicalRoutingInfos
     */
    public void setPhysicalRoutingInfos(List<PhysicalRoutingInfo> physicalRoutingInfos) {
        this.physicalRoutingInfos = physicalRoutingInfos;
    }

    /**
     * 获取
     * @return TighterToleranceLatency
     */
    public double getTighterToleranceLatency() {
        return TighterToleranceLatency;
    }

    /**
     * 设置
     * @param TighterToleranceLatency
     */
    public void setTighterToleranceLatency(double TighterToleranceLatency) {
        this.TighterToleranceLatency = TighterToleranceLatency;
    }

    /**
     * 设置当前微服务路径路由表
     * @param routing_tables_eachPath
     */
    public void setRouting_tables_eachPath(List<List<List<Object>>> routing_tables_eachPath) {
        this.Routing_tables_eachPath = routing_tables_eachPath;
    }

    /**
     * 获取当前微服务路径路由表
     * @return  Routing_tables_eachPath
     */
    public List<List<List<Object>>> getRouting_tables_eachPath() {
        return this.Routing_tables_eachPath;
    }
}
