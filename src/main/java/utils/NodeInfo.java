package utils;
import java.util.Map;
import java.util.Objects;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 20:45
 **/

public class NodeInfo {
    private int ServiceType; //当前DAG图该节点的微服务类型
    private ServiceTypeInfo serviceType; //当前DAG图该节点的微服务类型 主要区分有状态和无状态
    private Map<NodeInfo, Double> transitionProbabilities; //当前DAG图该节点的后继节点的信息，包括后继节点的DAG图编号，可以查看后继NodeInfo的Servicetype,以及以键值对形式保存了路由到后继Node的转发概率
    private double ArrivalRate_On_Node; //路由到当前节点服务的请求流数量用以计算Instance_To_Deploy
    private int Instance_To_Deploy; //在该节点当前到达率下需要部署的微服务实例数
    private int[] DeployedNode; //在不同编号物理节点的部署数量

    public NodeInfo() {
    }

    public NodeInfo(int serviceType, ServiceTypeInfo serviceTypeInfo, Map<NodeInfo, Double> transitions) {
        this.ServiceType = serviceType;
        this.serviceType = serviceTypeInfo;
        this.transitionProbabilities = transitions;
    }
    public NodeInfo(int ServiceType, ServiceTypeInfo serviceType, Map<NodeInfo, Double> transitionProbabilities, double ArrivalRate_On_Node, int Instance_To_Deploy, int[] DeployedNode) {
        this.ServiceType = ServiceType;
        this.serviceType = serviceType;
        this.transitionProbabilities = transitionProbabilities;
        this.ArrivalRate_On_Node = ArrivalRate_On_Node;
        this.Instance_To_Deploy = Instance_To_Deploy;
        this.DeployedNode = DeployedNode;
    }




    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return ServiceType == nodeInfo.ServiceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServiceType);
    }

    /**
     * 获取
     * @return serviceType
     */
    public int getServiceType() {
        return ServiceType;
    }

    /**
     * 设置
     * @param serviceType
     */
    public void setServiceType(int serviceType) {
        this.ServiceType = serviceType;
    }

    /**
     * 获取
     * @return transitionProbabilities
     */
    public Map<NodeInfo, Double> getTransitionProbabilities() {
        return transitionProbabilities;
    }

    /**
     * 设置
     * @param transitionProbabilities
     */
    public void setTransitionProbabilities(Map<NodeInfo, Double> transitionProbabilities) {
        this.transitionProbabilities = transitionProbabilities;
    }

    /**
     * 获取
     * @return ArrivalRate_On_Node
     */
    public double getArrivalRate_On_Node() {
        return ArrivalRate_On_Node;
    }

    /**
     * 设置
     * @param ArrivalRate_On_Node
     */
    public void setArrivalRate_On_Node(double ArrivalRate_On_Node) {
        this.ArrivalRate_On_Node = ArrivalRate_On_Node;
    }

    /**
     * 获取
     * @return Instance_To_Deploy
     */
    public double getInstance_To_Deploy() {
        return Instance_To_Deploy;
    }

    /**
     * 设置
     * @param Instance_To_Deploy
     */
    public void setInstance_To_Deploy(int Instance_To_Deploy) {
        this.Instance_To_Deploy = Instance_To_Deploy;
    }

    /**
     * 获取
     * @return DeployedNode
     */
    public int[] getDeployedNode() {
        return DeployedNode;
    }

    /**
     * 设置
     * @param DeployedNode
     */
    public void setDeployedNode(int[] DeployedNode) {
        this.DeployedNode = DeployedNode;
    }

    public String toString() {
        return "NodeInfo{serviceType = " + ServiceType + ", transitionProbabilities = " + transitionProbabilities + ", ArrivalRate_On_Node = " + ArrivalRate_On_Node + ", Instance_To_Deploy = " + Instance_To_Deploy + ", DeployedNode = " + DeployedNode + "}";
    }
}

