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
    private int[][] InstanceDeployOnNode; // 各个节点的部署结果 第一维为节点数，第二维为微服务种类
    private int[][] Routing_decision_Y; //路由决策变量，论文中的Y(t)
    private double[][] BandwidthResource; // 各个节点的部署结果 第一维为节点数，第二维为微服务种类


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
}
