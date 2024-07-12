package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-11 16:07
 **/
public class CurrentTimeApps {
    private ArrayList<AppPathInfo> appPathInfos;//保存当前微服务的所有app
    private ArrayList<Integer> ServiceInstanceNum; //当前时隙微服务实例的数量
    private int[][] InstanceDeployOnNode; // 各个节点的部署结果


    public CurrentTimeApps() {
    }

    public CurrentTimeApps(ArrayList<AppPathInfo> appPathInfos, ArrayList<Integer> ServiceInstanceNum, int[][] InstanceDeployOnNode) {
        this.appPathInfos = appPathInfos;
        this.ServiceInstanceNum = ServiceInstanceNum;
        this.InstanceDeployOnNode = InstanceDeployOnNode;
    }


    /**
     * 获取
     * @return appPathInfos
     */
    public ArrayList<AppPathInfo> getAppPathInfos() {
        return appPathInfos;
    }

    /**
     * 设置
     * @param appPathInfos
     */
    public void setAppPathInfos(ArrayList<AppPathInfo> appPathInfos) {
        this.appPathInfos = appPathInfos;
    }

    /**
     * 获取
     * @return ServiceInstanceNum
     */
    public ArrayList<Integer> getServiceInstanceNum() {
        return ServiceInstanceNum;
    }

    /**
     * 设置
     * @param ServiceInstanceNum
     */
    public void setServiceInstanceNum(ArrayList<Integer> ServiceInstanceNum) {
        this.ServiceInstanceNum = ServiceInstanceNum;
    }

    public String toString() {
        return "CurrentTimeApps{appPathInfos = " + appPathInfos + ", ServiceInstanceNum = " + ServiceInstanceNum + "}";
    }

    /**
     * 获取
     * @return InstanceDeployOnNode
     */
    public int[][] getInstanceDeployOnNode() {
        return InstanceDeployOnNode;
    }

    /**
     * 设置
     * @param InstanceDeployOnNode
     */
    public void setInstanceDeployOnNode(int[][] InstanceDeployOnNode) {
        this.InstanceDeployOnNode = InstanceDeployOnNode;
    }
}
