package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-11 16:07
 **/
public class CurrentTimeApps {
    private ArrayList<AppPathInfo> appPathInfos;//保存当前微服务的所有app
    private ArrayList<Integer> ServiceInstanceNum; //当前时隙微服务实例的数量
    private int[][] InstanceDeployOnNode; // 各个节点的部署结果 第一维为节点数，第二维为微服务种类
    private int[][] Routing_decision_Y; //路由决策变量，论文中的Y(t)
    private double[][] BandwidthResource; // 节点间剩余带宽
    private double[][] ArrivalRate_matrix; //节点上的到达率 第一维为节点数，第二维为微服务种类
    private double[][] dataTrans_NodeToNode; //节点i到节点j的数据通信总量


    public CurrentTimeApps() {
    }

    public CurrentTimeApps(ArrayList<AppPathInfo> appPathInfos, ArrayList<Integer> ServiceInstanceNum, int[][] InstanceDeployOnNode) {
        this.appPathInfos = appPathInfos;
        this.ServiceInstanceNum = ServiceInstanceNum;
        this.InstanceDeployOnNode = InstanceDeployOnNode;
    }

    public CurrentTimeApps(ArrayList<AppPathInfo> appPathInfos, ArrayList<Integer> ServiceInstanceNum, int[][] InstanceDeployOnNode, double[][] BandwidthResource) {
        this.appPathInfos = appPathInfos;
        this.ServiceInstanceNum = ServiceInstanceNum;
        this.InstanceDeployOnNode = InstanceDeployOnNode;
        this.BandwidthResource = BandwidthResource;
    }

    /**
     * 更新当前时隙下所有请求流app部署后的节点剩余带宽矩阵和节点的到达率矩阵
     * @return Routing_tables_eachPath
     */
    public double[][] genBandwidthResourceAndArrivalmatrix(App_Params app_params) {
        int forward_ms_type, forward_ms_node, backward_ms_type, backward_ms_node;
        double p;
        //重置带宽和到达率初始状态
        for (int i = 0; i < app_params.getPhysicalConnectionBandwidth().length; i++) {
            for (int j = 0; j < app_params.getPhysicalConnectionBandwidth()[i].length; j++) {
                this.BandwidthResource[i][j] = app_params.getPhysicalConnectionBandwidth()[i][j];
            }
        }
        for (int i = 0; i < this.ArrivalRate_matrix.length; i++) {
            Arrays.fill(this.ArrivalRate_matrix[i], 0.0);
        }
        for (int i = 0; i < this.dataTrans_NodeToNode.length; i++) {
            Arrays.fill(this.dataTrans_NodeToNode[i], 0.0);
        }
        //遍历当前时隙下所有app
        Iterator<AppPathInfo> it_appPathInfos = appPathInfos.iterator();
        while (it_appPathInfos.hasNext()){
            AppPathInfo appPathInfo = it_appPathInfos.next();
            Iterator<PathProbability> it_PathProbabilities = appPathInfo.getPathProbabilities().iterator();
            if (appPathInfo.getAppType() == 0){
                while (it_PathProbabilities.hasNext()){
                    PathProbability pathProbability = it_PathProbabilities.next();
                    double ArrivalRate = pathProbability.getArrivalRate();
                    List<List<List<Object>>> Routing_tables_eachPath = pathProbability.getRouting_tables_eachPath();
                    double[][] ArrivalRate_eachNode_asBackwardMs = new double[app_params.getNum_Server()][app_params.getNum_Microservice()]; //在当前路由表策略下的每个节点作为后继节点的到达率，[节点数][微服务数]
                    for (int i = 0; i < Routing_tables_eachPath.size(); i++) {
                        for (int j = 0; j < Routing_tables_eachPath.get(i).size(); j++) {
                            List<Object> routing_table = Routing_tables_eachPath.get(i).get(j);
                            forward_ms_type = (int)routing_table.get(0);
                            forward_ms_node = (int)routing_table.get(1);
                            backward_ms_type = (int)routing_table.get(2);
                            backward_ms_node = (int)routing_table.get(3);
                            p = (double)routing_table.get(4);
                            if (forward_ms_type == -1){
                                //首节点
                                double band_cost = ArrivalRate * p;
                                ArrivalRate_eachNode_asBackwardMs[backward_ms_node][backward_ms_type] = band_cost;
                                this.ArrivalRate_matrix[backward_ms_node][backward_ms_type] += ArrivalRate * p;
                            }else {
                                //非首节点
                                double band_cost = ArrivalRate_eachNode_asBackwardMs[forward_ms_node][forward_ms_type] * p;
                                ArrivalRate_eachNode_asBackwardMs[backward_ms_node][backward_ms_type] += band_cost;
                                this.ArrivalRate_matrix[backward_ms_node][backward_ms_type] += ArrivalRate_eachNode_asBackwardMs[forward_ms_node][forward_ms_type] * p;
                                if (forward_ms_node == backward_ms_node) continue;
                                this.BandwidthResource[forward_ms_node][backward_ms_node] -= band_cost;
                                this.BandwidthResource[backward_ms_node][forward_ms_node] -= band_cost;
                                this.dataTrans_NodeToNode[forward_ms_node][backward_ms_node] += ArrivalRate_eachNode_asBackwardMs[forward_ms_node][forward_ms_type] * p;
                                this.dataTrans_NodeToNode[backward_ms_node][forward_ms_node] += ArrivalRate_eachNode_asBackwardMs[forward_ms_node][forward_ms_type] * p;
                            }
                        }
                    }
                /*double s = 0;
                for (int i = 0; i < app_params.getNum_Server(); i++) {
                    s+=ArrivalRate_eachNode_asBackwardMs[i][pathProbability.getNodeInfos().get(Routing_tables_eachPath.size()-1).getServiceType()];
                }
                System.out.println(ArrivalRate);
                System.out.println(s);*/
                }
            } else if (appPathInfo.getAppType() == 1) {
                double[][] ArrivalRate_eachNode_asBackwardMs_parallel = new double[app_params.getNum_Server()][app_params.getNum_Microservice()]; //在当前路由表策略下的每个节点作为后继节点的到达率，[节点数][微服务数]
                while (it_PathProbabilities.hasNext()){
                    PathProbability pathProbability = it_PathProbabilities.next();
                    double ArrivalRate = pathProbability.getArrivalRate();
                    List<List<List<Object>>> Routing_tables_eachPath = pathProbability.getRouting_tables_eachPath();
                    for (int i = 0; i < Routing_tables_eachPath.size(); i++) {
                        for (int j = 0; j < Routing_tables_eachPath.get(i).size(); j++) {
                            List<Object> routing_table = Routing_tables_eachPath.get(i).get(j);
                            forward_ms_type = (int)routing_table.get(0);
                            forward_ms_node = (int)routing_table.get(1);
                            backward_ms_type = (int)routing_table.get(2);
                            backward_ms_node = (int)routing_table.get(3);
                            p = (double)routing_table.get(4);
                            ArrivalRate_eachNode_asBackwardMs_parallel[backward_ms_node][backward_ms_type] = ArrivalRate * p;
                            if (forward_ms_type == -1){
                                //首节点
                            }else {
                                //非首节点
                                double band_cost = ArrivalRate_eachNode_asBackwardMs_parallel[forward_ms_node][forward_ms_type] * p;
                                if (forward_ms_node == backward_ms_node) continue;
                                this.BandwidthResource[forward_ms_node][backward_ms_node] -= band_cost;
                                this.BandwidthResource[backward_ms_node][forward_ms_node] -= band_cost;
                                this.dataTrans_NodeToNode[forward_ms_node][backward_ms_node] += ArrivalRate_eachNode_asBackwardMs_parallel[forward_ms_node][forward_ms_type] * p;
                                this.dataTrans_NodeToNode[backward_ms_node][forward_ms_node] += ArrivalRate_eachNode_asBackwardMs_parallel[forward_ms_node][forward_ms_type] * p;
                            }
                        }
                    }
                }
                for (int i = 0; i < ArrivalRate_eachNode_asBackwardMs_parallel.length; i++) {
                    for (int j = 0; j < ArrivalRate_eachNode_asBackwardMs_parallel[0].length; j++) {
                        this.ArrivalRate_matrix[i][j] += ArrivalRate_eachNode_asBackwardMs_parallel[i][j];
                    }
                }
            }
        }
        System.out.println("当前时隙带宽:");
        for (int i = 0; i < BandwidthResource.length; i++) {
            System.out.println(Arrays.toString(BandwidthResource[i]));;
        }
        return this.BandwidthResource;
    }

    public int[][] genRouting_decision_Y(){
        int[][] Routing_decision_Y = new int[InstanceDeployOnNode.length][InstanceDeployOnNode[0].length];
        for (int i = 0; i < InstanceDeployOnNode.length; i++) {
            for (int j = 0; j < InstanceDeployOnNode[0].length; j++) {
                if (InstanceDeployOnNode[i][j] > 0){
                    Routing_decision_Y[i][j] = 1;
                }
            }
        }
        this.Routing_decision_Y = Routing_decision_Y;
        return this.Routing_decision_Y;
    }

    /**
     * 获取当前微服务的所有app ArrayList<AppPathInfo>
     * @return appPathInfos
     */
    public ArrayList<AppPathInfo> getAppPathInfos() {
        return appPathInfos;
    }

    /**
     * 设置当前微服务的所有app
     * @param appPathInfos
     */
    public void setAppPathInfos(ArrayList<AppPathInfo> appPathInfos) {
        this.appPathInfos = appPathInfos;
    }

    /**
     * 获取当前时隙微服务实例的数量
     * @return ServiceInstanceNum
     */
    public ArrayList<Integer> getServiceInstanceNum() {
        return ServiceInstanceNum;
    }

    /**
     * 设置当前时隙微服务实例的数量
     * @param ServiceInstanceNum
     */
    public void setServiceInstanceNum(ArrayList<Integer> ServiceInstanceNum) {
        this.ServiceInstanceNum = ServiceInstanceNum;
    }

    public String toString() {
        return "CurrentTimeApps{appPathInfos = " + appPathInfos + ", ServiceInstanceNum = " + ServiceInstanceNum + "}";
    }

    /**
     * 获取各个节点的部署结果 第一维为节点数，第二维为微服务种类
     * @return InstanceDeployOnNode
     */
    public int[][] getInstanceDeployOnNode() {
        return InstanceDeployOnNode;
    }

    /**
     * 设置各个节点的部署结果 第一维为节点数，第二维为微服务种类
     * @param InstanceDeployOnNode
     */
    public void setInstanceDeployOnNode(int[][] InstanceDeployOnNode) {
        this.InstanceDeployOnNode = InstanceDeployOnNode;
    }

    /**
     * 获取
     * @return BandwidthResource
     */
    public double[][] getBandwidthResource() {
        return BandwidthResource;
    }

    /**
     * 设置
     * @param BandwidthResource
     */
    public void setBandwidthResource(double[][] BandwidthResource) {
        this.BandwidthResource = BandwidthResource;
    }

    public double[][] getArrivalRate_matrix() {
        return ArrivalRate_matrix;
    }

    public void setArrivalRate_matrix(double[][] arrivalRate_matrix) {
        ArrivalRate_matrix = arrivalRate_matrix;
    }

    public double[][] getDataTrans_NodeToNode() {
        return dataTrans_NodeToNode;
    }

    public void setDataTrans_NodeToNode(double[][] dataTrans_NodeToNode) {
        this.dataTrans_NodeToNode = dataTrans_NodeToNode;
    }
}
