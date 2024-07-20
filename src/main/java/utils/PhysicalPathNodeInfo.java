package utils;

import java.util.Map;
import java.util.Objects;

/**
 * @author: Dior
 * @Desc:定义该类为物理路径访问当前物理节点的微服务种类以及数量,他是物理路径的一个属性，
 * 每个物理路径入口可以视为一个控制转发器，同一种dag路径存在着不同的物理路径，有着不同的物理路径入口，需要控制转发器按照一定比例转发请求到达率给入口微服务
 * 每条DAG path需要新建一个新的控制转发节点来存储转发概率
 * @create: 2024-06-14 16:58
 **/
public class PhysicalPathNodeInfo {
    private PhysicalNodeInfo physicalNode; //当前物理路径访问的物理节点，只需要访问该对象的NodeID
    private int ServiceType; //当前物理路径在当前次序访问该节点的微服务类型 可以从上级物理路径的上级DAG路径的NodeInfo的serviceType获取
    private ServiceTypeInfo serviceTypeState; //当前微服务类型 主要区分有状态和无状态
    private int ServiceNum; //当前物理路径在当前次序访问该节点的微服务类型的实例数量
    private Map<PhysicalPathNodeInfo, Double> transitionProbabilities; //当前物理路径的该微服务在经过该节点处理后的后继节点的信息，将该physicalNode上对ServiceNum数量的ServiceType实例处理完成后转发至后续微服务在不同节点的概率，其和为1，概率由实例数量比例决定
    //在该节点部署的的该微服务类型的实例数量 ServiceNum/NodeInfo.Instance_To_Deploy=


    public PhysicalPathNodeInfo() {
    }

    public PhysicalPathNodeInfo(PhysicalNodeInfo physicalNode, int ServiceType, ServiceTypeInfo serviceType, int ServiceNum, Map<PhysicalPathNodeInfo, Double> transitionProbabilities) {
        this.physicalNode = physicalNode;
        this.ServiceType = ServiceType;
        this.serviceTypeState = serviceType;
        this.ServiceNum = ServiceNum;
        this.transitionProbabilities = transitionProbabilities;
    }

    /**
     * 获取
     * @return physicalNode
     */
    public PhysicalNodeInfo getPhysicalNode() {
        return physicalNode;
    }

    /**
     * 设置
     * @param physicalNode
     */
    public void setPhysicalNode(PhysicalNodeInfo physicalNode) {
        this.physicalNode = physicalNode;
    }

    /**
     * 获取
     * @return ServiceType
     */
    public int getServiceType() {
        return ServiceType;
    }

    /**
     * 设置
     * @param ServiceType
     */
    public void setServiceType(int ServiceType) {
        this.ServiceType = ServiceType;
    }

    /**
     * 获取
     * @return ServiceNum
     */
    public int getServiceNum() {
        return ServiceNum;
    }

    /**
     * 设置
     * @param ServiceNum
     */
    public void setServiceNum(int ServiceNum) {
        this.ServiceNum = ServiceNum;
    }

    /**
     * 获取
     * @return transitionProbabilities
     */
    public Map<PhysicalPathNodeInfo, Double> getTransitionProbabilities() {
        return transitionProbabilities;
    }

    /**
     * 设置
     * @param transitionProbabilities
     */
    public void setTransitionProbabilities(Map<PhysicalPathNodeInfo, Double> transitionProbabilities) {
        this.transitionProbabilities = transitionProbabilities;
    }
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalPathNodeInfo physicalPathNodeInfo = (PhysicalPathNodeInfo) o;
        return ServiceType == physicalPathNodeInfo.ServiceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServiceType);
    }

    public String toString() {
        return "PhysicalPathNodeInfo{physicalNode = " + physicalNode + ", ServiceType = " + ServiceType + ", ServiceNum = " + ServiceNum + ", transitionProbabilities = " + transitionProbabilities + "}";
    }

    /**
     * 获取
     * @return serviceTypeState
     */
    public ServiceTypeInfo getServiceTypeState() {
        return serviceTypeState;
    }

    /**
     * 设置
     * @param serviceTypeState
     */
    public void setServiceTypeState(ServiceTypeInfo serviceTypeState) {
        this.serviceTypeState = serviceTypeState;
    }
}
