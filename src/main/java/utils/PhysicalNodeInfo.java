package utils;

import java.util.List;

/**
 * @author: Dior
 * @Desc:这个类是单独存在的，不属于别的类的属性，他存储的是当前节点所有微服务的种类以及数量
 * 如PhysicalPathNodeInfo中需要额外定义一个NodeID，为控制转发节点，其各属性均为null
 * @create: 2024-06-14 15:21
 **/
public class PhysicalNodeInfo {
    private int NodeID;//这边还可以将该数组拆分成微服务实例种类和数量两个属性，太复杂 感觉没必要
    private List<Integer> DeployServiceType;//这边还可以将该数组拆分成微服务实例种类和数量两个属性，太复杂 感觉没必要

    public PhysicalNodeInfo() {
    }

    public PhysicalNodeInfo(int NodeID, List<Integer> DeployServiceType) {
        this.NodeID = NodeID;
        this.DeployServiceType = DeployServiceType;
    }

    public PhysicalNodeInfo(int i) {
    }


    /**
     * 获取
     * @return DeployServiceType
     */
    public List<Integer> getDeployServiceType() {
        return DeployServiceType;
    }

    /**
     * 设置
     * @param DeployServiceType
     */
    public void setDeployServiceType(List<Integer> DeployServiceType) {
        this.DeployServiceType = DeployServiceType;
    }

    /**
     * 获取
     * @return NodeID
     */
    public int getNodeID() {
        return NodeID;
    }

    /**
     * 设置
     * @param NodeID
     */
    public void setNodeID(int NodeID) {
        this.NodeID = NodeID;
    }

    public String toString() {
        return "PhysicalNodeInfo{NodeID = " + NodeID + ", DeployServiceType = " + DeployServiceType + "}";
    }
}
