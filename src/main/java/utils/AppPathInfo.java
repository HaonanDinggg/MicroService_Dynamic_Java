package utils;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 17:16
 **/
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppPathInfo {

    private Map<Integer, NodeInfo> nodeInfos; //当前DAG图所有的节点信息
    private ArrayList<Integer> serviceInstanceNumApp;//当前app计算所有需要的实例数
    private List<PathProbability> pathProbabilities;
    //DAGPathInfo对象.pathProbabilities.get(index1第几条链).getprobability 获取当前index1路径的概率
    //DAGPathInfo对象.pathProbabilities.get(index1第几条链).get(index2第几个节点).getserviceType 获取当前index路径的第index2个节点的微服务类型 也可以获取当前节点的后继节点的信息
    private int Num_Dag_Node; // AppDAG图节点的数量
    private int Num_Dag_Edge; // AppDAG图边的数量
    private int Num_MicroService; // AppDAG图微服务种类数
    private int[][] adjMatrix ; // AppDAG图邻接矩阵
    private double ArrivalRate; // App到达率
    private double AppMaxToleranceLatency; // App最大容忍时延
    private int AppType; // DAG图类型 0 1区分概率转发和并行转发



    public AppPathInfo() {
    }
    public AppPathInfo(Map<Integer, NodeInfo> nodeInfos, List<PathProbability> pathProbabilities, int Num_Dag_Node, int Num_Dag_Edge, int Num_MicroService, int[][] adjMatrix, double ArrivalRate, double AppMaxToleranceLatency, int AppType) {
        this.nodeInfos = nodeInfos;
        this.pathProbabilities = pathProbabilities;
        this.Num_Dag_Node = Num_Dag_Node;
        this.Num_Dag_Edge = Num_Dag_Edge;
        this.Num_MicroService = Num_MicroService;
        this.adjMatrix = adjMatrix;
        this.ArrivalRate = ArrivalRate;
        this.AppMaxToleranceLatency = AppMaxToleranceLatency;
        this.AppType = AppType;
    }

    public AppPathInfo(Map<Integer, NodeInfo> nodeInfos, ArrayList<Integer> serviceInstanceNumApp, List<PathProbability> pathProbabilities, int Num_Dag_Node, int Num_Dag_Edge, int Num_MicroService, int[][] adjMatrix, double ArrivalRate, double AppMaxToleranceLatency, int AppType) {
        this.nodeInfos = nodeInfos;
        this.serviceInstanceNumApp = serviceInstanceNumApp;
        this.pathProbabilities = pathProbabilities;
        this.Num_Dag_Node = Num_Dag_Node;
        this.Num_Dag_Edge = Num_Dag_Edge;
        this.Num_MicroService = Num_MicroService;
        this.adjMatrix = adjMatrix;
        this.ArrivalRate = ArrivalRate;
        this.AppMaxToleranceLatency = AppMaxToleranceLatency;
        this.AppType = AppType;
    }


    /**
     * 获取
     * @return nodeInfos
     */
    public Map<Integer, NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    /**
     * 设置
     * @param nodeInfos
     */
    public void setNodeInfos(Map<Integer, NodeInfo> nodeInfos) {
        this.nodeInfos = nodeInfos;
    }

    /**
     * 获取
     * @return pathProbabilities
     */
    public List<PathProbability> getPathProbabilities() {
        return pathProbabilities;
    }

    /**
     * 设置
     * @param pathProbabilities
     */
    public void setPathProbabilities(List<PathProbability> pathProbabilities) {
        this.pathProbabilities = pathProbabilities;
    }

    /**
     * 获取
     * @return Num_Dag_Node
     */
    public int getNum_Dag_Node() {
        return Num_Dag_Node;
    }

    /**
     * 设置
     * @param Num_Dag_Node
     */
    public void setNum_Dag_Node(int Num_Dag_Node) {
        this.Num_Dag_Node = Num_Dag_Node;
    }

    /**
     * 获取
     * @return Num_Dag_Edge
     */
    public int getNum_Dag_Edge() {
        return Num_Dag_Edge;
    }

    /**
     * 设置
     * @param Num_Dag_Edge
     */
    public void setNum_Dag_Edge(int Num_Dag_Edge) {
        this.Num_Dag_Edge = Num_Dag_Edge;
    }

    /**
     * 获取
     * @return Num_MicroService
     */
    public int getNum_MicroService() {
        return Num_MicroService;
    }

    /**
     * 设置
     * @param Num_MicroService
     */
    public void setNum_MicroService(int Num_MicroService) {
        this.Num_MicroService = Num_MicroService;
    }

    /**
     * 获取
     * @return adjMatrix
     */
    public int[][] getAdjMatrix() {
        return adjMatrix;
    }

    /**
     * 设置
     * @param adjMatrix
     */
    public void setAdjMatrix(int[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
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

    /**
     * 获取
     * @return AppType
     */
    public int getAppType() {
        return AppType;
    }

    /**
     * 设置
     * @param AppType
     */
    public void setAppType(int AppType) {
        this.AppType = AppType;
    }

    /**
     * 获取
     * @return AppMaxToleranceLatency
     */
    public double getAppMaxToleranceLatency() {
        return AppMaxToleranceLatency;
    }

    /**
     * 设置
     * @param AppMaxToleranceLatency
     */
    public void setAppMaxToleranceLatency(double AppMaxToleranceLatency) {
        this.AppMaxToleranceLatency = AppMaxToleranceLatency;
    }

    public String toString() {
        return "AppPathInfo{nodeInfos = " + nodeInfos + ", pathProbabilities = " + pathProbabilities + ", Num_Dag_Node = " + Num_Dag_Node + ", Num_Dag_Edge = " + Num_Dag_Edge + ", Num_MicroService = " + Num_MicroService + ", adjMatrix = " + adjMatrix + ", ArrivalRate = " + ArrivalRate + ", AppMaxToleranceLatency = " + AppMaxToleranceLatency + ", AppType = " + AppType + "}";
    }


    /**
     * 获取
     * @return serviceInstanceNumApp
     */
    public ArrayList<Integer> getServiceInstanceNumApp() {
        return serviceInstanceNumApp;
    }

    /**
     * 设置
     * @param serviceInstanceNumApp
     */
    public void setServiceInstanceNumApp(ArrayList<Integer> serviceInstanceNumApp) {
        this.serviceInstanceNumApp = serviceInstanceNumApp;
    }
}
