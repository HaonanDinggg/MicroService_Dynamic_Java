package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.stream.Collectors;


import static utils.WaitingTime.calculateMicroserviceNodeAverageServiceTime;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 14:58
 **/

public class Main_Algorithm {
    //    private static int[] indegree;
//    // 记录已经遍历的节点
//    private static boolean[] visited;
//    // 记录拓扑排序的结果
//    private static List<Integer> result;


    public static App_Params init() {
        App_Params appParams = new App_Params();
        appParams.setNum_Server(600);
        appParams.setNum_Microservice(10);
        appParams.setNum_Application(30);
        appParams.setNum_Time_Slot(5);
        appParams.setNum_CPU_Core(50);
        appParams.setMAX1(100);
        appParams.setAvgArrivalRateDataSize(1);
        appParams.setDataBaseCommunicationDelay(0.05);//不知道要不要写到appParams中作为常量还是后续要详细计算 目前当常量考虑 //0.1
        appParams.setRoundRobinParam(2);
        appParams.setEqualizationCoefficient(0.8);
        appParams.setAvgNetworkResourceUtilization(0.9);
        appParams.setApp_Num(new int[]{800,800});
        appParams.setTTL_Max_Tolerance_Latency_Range(new int[]{5,5});
        appParams.setUnit_Rate_Bandwidth_Range(new double[]{0.11,2});
        appParams.setAverage_Arrival_Rate_Range(new int[]{8,8});
        appParams.setNum_Node_Range(new int[]{2,6});
        appParams.setNum_Edge_Range(new int[]{1,6});
        appParams.setDAG_Category_Range(new int[]{0,1});
        appParams.setNum_Apps_Timeslot_Range(new int[]{1,100});
        appParams.setMicroservice_Type_CPU(new int[]{1,5});
        appParams.setMicroservice_Type_Memory(new int[]{1,5});
        appParams.setLowest_Communication_Latency(0.05);
        appParams.setHighest_Communication_Latency(0.1);
        //成本
        appParams.setServerScalingCost(100);
        appParams.setMicroservice_Type_ServiceScalingCost(new int[]{6,14});
        appParams.setMicroservice_Type_Migration_Cost(new double[]{2,4});

        appParams.setLowest_Bandwidth_Capacity(10);
        appParams.setHighest_Bandwidth_Capacity(30);
        appParams.setLowest_Microservice_Bandwidth_Requirement(1);
        appParams.setHighest_Microservice_Bandwidth_Requirement(2);
        appParams.setLowest_Microservice_Type_Unit_Process_Ability(3);
        appParams.setHighest_Microservice_Type_Unit_Process_Ability(8);
        appParams.setAvgPhysicalConnectionDelay((appParams.getHighest_Communication_Latency() + appParams.getLowest_Communication_Latency())/2);
        appParams.setAvgPhysicalConnectionBandwidth((appParams.getHighest_Bandwidth_Capacity() + appParams.getLowest_Bandwidth_Capacity())/2);
        //写方法构建几个其他的constant属性
        appParams.InitConstant();
        appParams.CreateServiceList();
        return appParams;
    }

    public static void main(String[] args) {

        Random r = new Random(214);
        App_Params appParams = init();
        String filePathParams = String.format("D:\\华科工作\\实验室工作\\胡毅学长动态\\Dynamic_Java\\appParams\\app_params.json");
        File fileParams = new File(filePathParams);
        fileParams.getParentFile().mkdirs();
        // 确保目录存在
        try {
            saveToJsonFile(appParams, filePathParams);
            //System.out.println("对象已成功保存到 app_params.json 文件中。");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(appParams);
        //需要初始化所有的ServiceTypeInfo 共用一套微服务信息
        ArrayList<CurrentTimeApps> alltimeApp = GenerateApp.CreateAlltimeApp(appParams.getNum_Time_Slot(),appParams,r);

        for (int i = 0; i < appParams.getNum_Microservice(); i++) {
            int serviceProcessingRate = appParams.getServiceTypeInfos().get(i).getServiceProcessingRate();
            System.out.println("微服务" + appParams.getServiceTypeInfos().get(i).getServiceID() + "的单位实例处理能力" + serviceProcessingRate);
        }
        //后面开始准备计算实例需求
        alltimeApp = InstanceCalculator.CalculateInstance(appParams,alltimeApp);
        System.out.println("====ar2====");


        //准备部署实例
        alltimeApp = InstanceDeploy.DeployInstance(appParams,alltimeApp);

        //准备进行迁移
        alltimeApp = Migration.Migration(appParams,alltimeApp);

        //时延以及成功率计算
        for(int time = 0; time < alltimeApp.size(); time++){
            ArrayList<AppPathInfo> currentAppList = alltimeApp.get(time).getAppPathInfos();
            double[][] Arrival_matrix = alltimeApp.get(time).getArrivalRate_matrix();
            int[][] instanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
            double[][] dataTrans_NodeToNode = alltimeApp.get(time).getDataTrans_NodeToNode();
            double avgLatency = 0;
            int successNum = currentAppList.size();
            for (AppPathInfo app : currentAppList) {
                List<PathProbability> pathProbabilities = app.getPathProbabilities();
                double maxLatencyApp = 0;
                for (PathProbability pathProbability : pathProbabilities) {
                    List<NodeInfo> nodeInfos = pathProbability.getNodeInfos();
                    List<List<List<Object>>>routing_tables_eachPath = pathProbability.getRouting_tables_eachPath();
                    double maxLatencyPath = 0;
                    for (int index = 0; index < nodeInfos.size(); index++) {
                        NodeInfo nodeInfo = nodeInfos.get(index);
                        int serviceID = nodeInfo.getServiceType();
                        int currentServiceProcessingRate = appParams.getServiceTypeInfos().get(serviceID).getServiceProcessingRate();
                        //只区分尾节点和其余节点 其余节点需要计算节点上排队+处理，以及传播，尾节点只需要计算排队+处理
                        if (index == nodeInfos.size() - 1) {
                            //如果m是尾节点
                            List<List<Object>> microService = routing_tables_eachPath.get(index);
                            double maxLatencyMs = 0;
                            for (List<Object> transRoute : microService) {
                                double currentLinkLatency = 0;
                                int startService = (int) transRoute.get(0);
                                int startNode = (int) transRoute.get(1);
                                int endService = (int) transRoute.get(2);
                                int endNode = (int) transRoute.get(3);
                                double transpro = (double) transRoute.get(4);
                                currentLinkLatency = calculateMicroserviceNodeAverageServiceTime(Arrival_matrix[endNode][endService],instanceDeployOnNode[endNode][endService],currentServiceProcessingRate);
                                if(nodeInfo.getServiceTypeState().getServiceState() == 1){
                                    currentLinkLatency += appParams.getDataBaseCommunicationDelay();
                                }
                                if(currentLinkLatency > maxLatencyMs){
                                    maxLatencyMs = currentLinkLatency;
                                }
                            }
                            maxLatencyPath += maxLatencyMs;

                        } else {
                            List<List<Object>> microService = routing_tables_eachPath.get(index+1);
                            double maxLatencyMs = 0;
                            for (List<Object> transRoute : microService) {
                                double currentLinkLatency = 0;
                                int startService = (int) transRoute.get(0);
                                int startNode = (int) transRoute.get(1);
                                int endService = (int) transRoute.get(2);
                                int endNode = (int) transRoute.get(3);
                                double transpro = (double) transRoute.get(4);
                                currentLinkLatency = appParams.getPhysicalConnectionDelay()[startNode][endNode]
                                        + calculateMicroserviceNodeAverageServiceTime(Arrival_matrix[startNode][startService],instanceDeployOnNode[startNode][startService],currentServiceProcessingRate)
                                        + dataTrans_NodeToNode[startNode][endNode]/appParams.getPhysicalConnectionBandwidth()[startNode][endNode] ;
                                if(nodeInfo.getServiceTypeState().getServiceState() == 1){
                                    currentLinkLatency += appParams.getDataBaseCommunicationDelay();
                                }
                                if(currentLinkLatency > maxLatencyMs){
                                    maxLatencyMs = currentLinkLatency;
                                }
                            }
                            maxLatencyPath += maxLatencyMs;
                        }
                    }
                    if(maxLatencyPath > maxLatencyApp){
                        maxLatencyApp = maxLatencyPath;
                    }
                }
                //System.out.println("当前app时延为 "+maxLatencyApp);
                avgLatency += maxLatencyApp;
                if(maxLatencyApp > app.getAppMaxToleranceLatency()){
                    successNum--;
                }
            }

            System.out.println("当前时隙" + time+1 + "app平均时延为 "+avgLatency/currentAppList.size());
            System.out.println("当前时隙" + time+1 + "app服务成功率为 "+successNum/currentAppList.size());
        }
        //
        //迁移成本计算
        double avg= 0.0;
        for(int time = 0; time < alltimeApp.size()-1; time++){
            int[][] currentInstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
            int[][] comingInstanceDeployOnNode = alltimeApp.get(time+1).getInstanceDeployOnNode();
            int serverCost = appParams.getServerScalingCost()*countNewlyDeployedNodes(currentInstanceDeployOnNode,comingInstanceDeployOnNode);
            List<Integer> serviceCost = calculateDeploymentIncrease(currentInstanceDeployOnNode,comingInstanceDeployOnNode);//考虑伸缩成本
            int serviceCostSum = 0;
            for (int i = 0; i < serviceCost.size(); i++) {
                serviceCostSum += appParams.getmicroService_Type_ServiceScalingCost().get(i)*serviceCost.get(i);
            }

            List<Double> migrationCost = alltimeApp.get(time+1).getMigrationCost();
            double migrationCostSum = 0;
            for (int i = 0; i < migrationCost.size(); i++) {
                migrationCostSum += appParams.getmicroService_Type_Migration_Cost().get(i)*migrationCost.get(i);
            }
            double costSum = migrationCostSum + serverCost + serviceCostSum;
//            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的服务器伸缩成本为"+serverCost);
//            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的微服务伸缩成本为"+serviceCost);
//            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的微服务伸缩总成本为"+serviceCostSum);
//            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的迁移成本为"+migrationCost);
//            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的迁移总成本为"+migrationCostSum);
            System.out.println("当前时隙"+(time+1)+"与时隙 "+(time+2)+"的总成本为"+costSum);
            avg+=costSum;
        }
        avg/=alltimeApp.size()-1;
        System.out.println("平均" + avg);
        //网络公平指数
        for(int time = 0; time < alltimeApp.size(); time++){
            int nodeCoreNum = appParams.getNum_CPU_Core();
            int[][] currentInstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
            double[][] bandwidthResource = alltimeApp.get(time).getBandwidthResource();
            int[][] physicalConnectionBandwidth = appParams.getPhysicalConnectionBandwidth();
            double serverNetworkFairnessIndex = calServerNetworkFairnessIndex(currentInstanceDeployOnNode,nodeCoreNum);
            double bandNetworkFairnessIndex = calBandNetworkFairnessIndex(physicalConnectionBandwidth,bandwidthResource);
            //System.out.println("当前时隙"+(time+1)+"的服务器公平指数"+serverNetworkFairnessIndex);
            //System.out.println("当前时隙"+(time+1)+"的带宽公平指数"+bandNetworkFairnessIndex);
            System.out.println("当前时隙"+(time+1)+"的公平指数"+(0.5*serverNetworkFairnessIndex+0.5*bandNetworkFairnessIndex));

            System.out.println("当前时隙"+(time+1)+"节点利用率的公平指数"+serverNetworkFairnessIndex);
            System.out.println("当前时隙"+(time+1)+"带宽利用率的公平指数"+bandNetworkFairnessIndex);
        }
    }


    public static double calBandNetworkFairnessIndex(int[][] physicalConnectionBandwidth, double[][] bandwidthResource) {
        double bandNetworkFairnessIndex = 0;
        int numNodes1 = physicalConnectionBandwidth.length;
        int numNodes2 = physicalConnectionBandwidth[0].length;
        int activeBandNum = 0;
        double sumU = 0;
        double sumUSq = 0;
        for (int i = 0; i < numNodes1; i++) {
            for (int j = i; j < numNodes2; j++) {
                if(bandwidthResource[i][j] < physicalConnectionBandwidth[i][j]){
                    activeBandNum++;
                    sumU += (physicalConnectionBandwidth[i][j]-bandwidthResource[i][j])/physicalConnectionBandwidth[i][j];
                    sumUSq += (physicalConnectionBandwidth[i][j]-bandwidthResource[i][j])/physicalConnectionBandwidth[i][j]*(physicalConnectionBandwidth[i][j]-bandwidthResource[i][j])/physicalConnectionBandwidth[i][j];
                }
            }
        }
        bandNetworkFairnessIndex = sumU*sumU/sumUSq/activeBandNum;
        return bandNetworkFairnessIndex;
    }
    public static double calServerNetworkFairnessIndex(int[][] currentInstanceDeployOnNode, int nodeCoreNum) {
        double serverNetworkFairnessIndex = 0;
        int numNodes = currentInstanceDeployOnNode.length;
        int numServices = currentInstanceDeployOnNode[0].length;
        int activeNodeNum = 0;
        double sumU = 0;
        double sumUSq = 0;
        for (int i = 0; i < numNodes; i++) {
            int nodeInstanceNum =  0;
            for (int j = 0; j < numServices; j++) {
                nodeInstanceNum += currentInstanceDeployOnNode[i][j];
            }
            if(nodeInstanceNum>0){
                activeNodeNum++;
                double utilizationNode = (double)nodeInstanceNum/nodeCoreNum;
                sumU += utilizationNode;
                sumUSq += utilizationNode * utilizationNode;
            }
        }
        serverNetworkFairnessIndex = sumU*sumU/sumUSq/activeNodeNum;
        return serverNetworkFairnessIndex;
    }



    // 统计从未有任何服务部署到有服务部署的节点数量
    public static int countNewlyDeployedNodes(int[][] current, int[][] coming) {
        int newlyDeployedNodes = 0;
        int numNodes = current.length;
        int numServices = current[0].length;

        for (int j = 0; j < numNodes; j++) {
            boolean hadNoDeployment = true;
            boolean hasNewDeployment = false;
            for (int i = 0; i < numServices; i++) {
                if (current[j][i] != 0) {
                    hadNoDeployment = false;
                    break;
                }
            }
            if (hadNoDeployment) {
                for (int i = 0; i < numServices; i++) {
                    if (coming[j][i] > 0) {
                        hasNewDeployment = true;
                        break;
                    }
                }
            }
            if (hadNoDeployment && hasNewDeployment) {
                newlyDeployedNodes++;
            }
        }

        return newlyDeployedNodes;
    }

    // 统计每种服务在所有节点上增加的部署数量
    public static List<Integer> calculateDeploymentIncrease(int[][] current, int[][] coming) {
        List<Integer> deploymentIncrease = new ArrayList<>();
        int numNodes = current.length;
        int numServices = current[0].length;

        for (int i = 0; i < numServices; i++) {
            int totalIncrease = 0;
            for (int j = 0; j < numNodes; j++) {
                if(coming[j][i] - current[j][i] > 0){
                    int increase = coming[j][i] - current[j][i];
                    totalIncrease += increase;
                }
            }
            deploymentIncrease.add(totalIncrease);
        }

        return deploymentIncrease;
    }


    /**
     * @Description : 将 DAGPathInfo 对象序列化为 JSON 文件并保存。
     * @Author : Dior
     *  * @param obj
     * @param fileName
     * @return : void
     * @Date : 2024/7/2
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    private static void saveToJsonFile(Object obj, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(fileName), obj);
    }

}
