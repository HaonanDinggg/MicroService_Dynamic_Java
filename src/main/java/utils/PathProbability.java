package utils;

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
     * 获取
     * @return probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * 设置
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
}
