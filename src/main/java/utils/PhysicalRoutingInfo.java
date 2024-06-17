package utils;

import javax.xml.ws.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-14 15:19
 **/
//该对象应该是PathProbability的一个属性中的一个元素
public class PhysicalRoutingInfo {
    private List<PhysicalPathNodeInfo> RoutingServiceType; //存储物理路由路径上的各个节点以及访问该节点上的微服务的种类信息 任何物理路径的第一个元素均为控制转发器
    private double probability; //存储该物理路由路径的概率 它是由PhysicalPathNodeInfo中的transitionProbabilities计算获得的

    public void CalProbability(){
        double Currentprobability = 1;
        for(int i = 0;i < this.RoutingServiceType.size()+1;i++){
            PhysicalPathNodeInfo currentPhysicalPathNode = this.RoutingServiceType.get(i);
            PhysicalPathNodeInfo nextPhysicalPathNode = this.RoutingServiceType.get(i+1);
            Currentprobability *= currentPhysicalPathNode.getTransitionProbabilities().get(nextPhysicalPathNode);
        }
        this.probability = Currentprobability;
    }

    public PhysicalRoutingInfo() {
    }

    public PhysicalRoutingInfo(List<PhysicalPathNodeInfo> RoutingServiceType, double probability) {
        this.RoutingServiceType = RoutingServiceType;
        this.probability = probability;
    }

    /**
     * 获取
     * @return RoutingServiceType
     */
    public List<PhysicalPathNodeInfo> getRoutingServiceType() {
        return RoutingServiceType;
    }

    /**
     * 设置
     * @param RoutingServiceType
     */
    public void setRoutingServiceType(List<PhysicalPathNodeInfo> RoutingServiceType) {
        this.RoutingServiceType = RoutingServiceType;
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

    public String toString() {
        return "PhysicalRoutingInfo{RoutingServiceType = " + RoutingServiceType + ", probability = " + probability + "}";
    }
}
