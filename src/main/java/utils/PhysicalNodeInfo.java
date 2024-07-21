package utils;

import javax.xml.soap.Node;
import java.util.List;
import java.util.Map;

/**
 * @author: Dior
 * @Desc:这个类是单独存在的，不属于别的类的属性，他存储的是当前节点所有微服务的种类以及数量
 * 如PhysicalPathNodeInfo中需要额外定义一个NodeID，为控制转发节点，其各属性均为null
 * @create: 2024-06-14 15:21
 **/
public class PhysicalNodeInfo {
    private int NodeID;//物理节点id
    private int UsedCPUResourcce;//节点已经占用的cpu资源
    private int FreeCPUResourcce;//节点空余的cpu资源
    private int UsedMemoryResourcce;//节点已经占用的memory资源
    private int FreeMemoryResourcce;//节点空余的memory资源
    private List<Integer> DeployServiceType;//这边还可以将该数组拆分成微服务实例种类和数量两个属性，太复杂 感觉没必要



    public PhysicalNodeInfo(int i) {
        //虚拟首转发器,i = -1
        this.NodeID = i;
    }

    public PhysicalNodeInfo() {
    }

    public PhysicalNodeInfo(int NodeID, int UsedCPUResourcce, int FreeCPUResourcce, int UsedMemoryResourcce, int FreeMemoryResourcce, List<Integer> DeployServiceType) {
        this.NodeID = NodeID;
        this.UsedCPUResourcce = UsedCPUResourcce;
        this.FreeCPUResourcce = FreeCPUResourcce;
        this.UsedMemoryResourcce = UsedMemoryResourcce;
        this.FreeMemoryResourcce = FreeMemoryResourcce;
        this.DeployServiceType = DeployServiceType;
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

    /**
     * 获取
     * @return UsedCPUResourcce
     */
    public int getUsedCPUResourcce() {
        return UsedCPUResourcce;
    }

    /**
     * 设置
     * @param UsedCPUResourcce
     */
    public void setUsedCPUResourcce(int UsedCPUResourcce) {
        this.UsedCPUResourcce = UsedCPUResourcce;
    }

    /**
     * 获取
     * @return FreeCPUResourcce
     */
    public int getFreeCPUResourcce() {
        return FreeCPUResourcce;
    }

    /**
     * 设置
     * @param FreeCPUResourcce
     */
    public void setFreeCPUResourcce(int FreeCPUResourcce) {
        this.FreeCPUResourcce = FreeCPUResourcce;
    }

    /**
     * 获取
     * @return UsedMemoryResourcce
     */
    public int getUsedMemoryResourcce() {
        return UsedMemoryResourcce;
    }

    /**
     * 设置
     * @param UsedMemoryResourcce
     */
    public void setUsedMemoryResourcce(int UsedMemoryResourcce) {
        this.UsedMemoryResourcce = UsedMemoryResourcce;
    }

    /**
     * 获取
     * @return FreeMemoryResourcce
     */
    public int getFreeMemoryResourcce() {
        return FreeMemoryResourcce;
    }

    /**
     * 设置
     * @param FreeMemoryResourcce
     */
    public void setFreeMemoryResourcce(int FreeMemoryResourcce) {
        this.FreeMemoryResourcce = FreeMemoryResourcce;
    }

    /**
     * 获取
     * @return DeployServiceType
     */
    public List<Integer> getDeployServiceType() {
        return DeployServiceType;
    }

    public String toString() {
        return "PhysicalNodeInfo{NodeID = " + NodeID + ", UsedCPUResourcce = " + UsedCPUResourcce + ", FreeCPUResourcce = " + FreeCPUResourcce + ", UsedMemoryResourcce = " + UsedMemoryResourcce + ", FreeMemoryResourcce = " + FreeMemoryResourcce + ", DeployServiceType = " + DeployServiceType + "}";
    }
}
