package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;


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
        appParams.setNum_Server(10);
        appParams.setNum_Microservice(50);
        appParams.setNum_Application(30);
        appParams.setNum_Time_Slot(20);
        appParams.setNum_CPU_Core(30);
        appParams.setMAX1(100);
        appParams.setAvgArrivalRateDataSize(1);
        appParams.setDataBaseCommunicationDelay(0);//不知道要不要写到appParams中作为常量还是后续要详细计算 目前当常量考虑 //0.1
        appParams.setRoundRobinParam(2);
        appParams.setApp_Num(new int[]{1,5});
        appParams.setTTL_Max_Tolerance_Latency_Range(new int[]{5,10});
        appParams.setUnit_Rate_Bandwidth_Range(new double[]{0.11,2});
        appParams.setAverage_Arrival_Rate_Range(new int[]{1,10});
        appParams.setNum_Node_Range(new int[]{2,6});
        appParams.setNum_Edge_Range(new int[]{1,6});
        appParams.setDAG_Category_Range(new int[]{0,1});
        appParams.setNum_Apps_Timeslot_Range(new int[]{1,100});
        appParams.setMicroservice_Type_CPU(new int[]{1,5});
        appParams.setMicroservice_Type_Memory(new int[]{1,5});
        appParams.setLowest_Communication_Latency(0.5);
        appParams.setHighest_Communication_Latency(1);
        appParams.setLowest_Bandwidth_Capacity(50);
        appParams.setHighest_Bandwidth_Capacity(100);
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
        int timeslot = 5;
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
        ArrayList<CurrentTimeApps> alltimeApp = GenerateApp.CreateAlltimeApp(timeslot,appParams,r);

        //后面开始准备计算实例需求
        alltimeApp = InstanceCalculator.CalculateInstance(appParams,alltimeApp);
        System.out.println("====ar2====");

        //准备部署实例
        for(int time = 0; time < alltimeApp.size(); time++){
            double equalizationCoefficient = 0.9;//公平指数下限
            ArrayList<Integer> NowServiceInstanceNum = alltimeApp.get(time).getServiceInstanceNum();//获取当前时隙的app情况
            ArrayList<Integer> PastServiceInstanceNum = new ArrayList<>(appParams.getNum_Microservice());//获取前一个时隙后的app情况
            if(time == 0) {
                System.out.println("进行第"+(time+1)+"个时隙的实例部署");
                //在第一个时隙初始化部署矩阵
                int[][] InstanceDeployOnNode = new int[appParams.getNum_Server()][appParams.getNum_Microservice()];
                alltimeApp.get(time).setInstanceDeployOnNode(InstanceDeployOnNode);
                //在第一个时隙，其不存在前一个时隙的部署,即所有均为0
                for (int i = 0; i < appParams.getNum_Microservice(); i++) {
                    PastServiceInstanceNum.add(0); // 或者任何其他默认值
                }
                //ArrayList<Integer> incrementInstances = new ArrayList<>();
                //将该时隙微服务数量按升序排序
                Map<Integer,Integer> sortedNowServiceInstanceNum = ArrayToSortedMap(NowServiceInstanceNum);
                Map<Integer,Integer> sortedPastServiceInstanceNum = ArrayToSortedMap(PastServiceInstanceNum);
                // 打印排序后的结果
                for (Map.Entry<Integer, Integer> nowEntry : sortedNowServiceInstanceNum.entrySet()) {
                    int ServiceID = nowEntry.getKey();
                    int nowServiceInstanceNum = nowEntry.getValue();
                    Map.Entry<Integer, Integer> PastEntry = getEntryByKey(sortedPastServiceInstanceNum, ServiceID);
                    int pastServiceInstanceNum = PastEntry.getValue();
                    int incrementInstanceNum = nowServiceInstanceNum - pastServiceInstanceNum;
                    //如果是第一个时隙 那么直接采用贪心的部署方法 部署当前时隙需要的所有微服务实例
                    InitDeployMsOnNode(alltimeApp.get(time).getInstanceDeployOnNode(),appParams,ServiceID,incrementInstanceNum);
                }
                System.out.println("当前"+time+"时隙部署结果"+Arrays.deepToString(InstanceDeployOnNode));
                System.out.println();
            }else {
                System.out.println("进行第"+(time+1)+"个时隙的实例部署");
                PastServiceInstanceNum = alltimeApp.get(time-1).getServiceInstanceNum();//获取前一个时隙后的app情况
                //在第一个时隙初始化部署矩阵
                int[][] InstanceDeployOnNode = alltimeApp.get(time-1).getInstanceDeployOnNode();
                double redundantFactor = calculateRedundantFactor(InstanceDeployOnNode,NowServiceInstanceNum);//当前时隙的冗余因子
                //将该时隙微服务数量按升序排序
                Map<Integer,Integer> sortedNowServiceInstanceNum = ArrayToSortedMap(NowServiceInstanceNum);
                Map<Integer,Integer> sortedPastServiceInstanceNum = ArrayToSortedMap(PastServiceInstanceNum);
                // 打印排序后的结果
                for (Map.Entry<Integer, Integer> nowEntry : sortedNowServiceInstanceNum.entrySet()) {
                    int ServiceID = nowEntry.getKey();
                    int nowServiceInstanceNum = nowEntry.getValue();
                    // 获取指定键的Entry
                    Map.Entry<Integer, Integer> PastEntry = getEntryByKey(sortedPastServiceInstanceNum, ServiceID);
                    int pastServiceInstanceNum = PastEntry.getValue();

                    if(nowEntry.getValue() == 0){
                        //如果该时隙下不存在该微服务的实例 回收微服务m的所有实例
                        for(int node = 0; node < appParams.getNum_Server(); node++){
                            InstanceDeployOnNode[node][ServiceID] = 0;
                        }
                        continue;
                    }
                    if(nowServiceInstanceNum < pastServiceInstanceNum){
                        int pastDeployedServiceNum = 0;
                        Map<Integer,Integer> serviceDeloyedNum = new HashMap<>();
                        for(int node = 0; node < appParams.getNum_Server(); node++){
                            pastDeployedServiceNum += InstanceDeployOnNode[node][ServiceID];
                            if(InstanceDeployOnNode[node][ServiceID] > 0){
                                serviceDeloyedNum.put(node,InstanceDeployOnNode[node][ServiceID]);
                            }
                        }
                        int actualDeployedServiceNum = nowServiceInstanceNum + (int) redundantFactor*(nowServiceInstanceNum-pastDeployedServiceNum);//实际需要部署的实例
                        int deployedServiceNumToReduce = pastDeployedServiceNum - actualDeployedServiceNum;
                        sortByValueAscending(serviceDeloyedNum);
                        for (Map.Entry<Integer, Integer> entry : serviceDeloyedNum.entrySet()) {
                            //遍历所有包含m的节点 升序排序，先清除小的
                            int nodeID = entry.getKey();
                            int result1 = appParams.getNum_CPU_Core(); //存储在当前时隙该节点可用的资源数量
                            //计算当前节点可用资源数量
                            for (int service = 0; service < appParams.getNum_Server(); service++) {
                                result1 -= InstanceDeployOnNode[nodeID][service];
                            }
                            if(result1 > deployedServiceNumToReduce){
                                InstanceDeployOnNode[nodeID][ServiceID] -= deployedServiceNumToReduce;
                                deployedServiceNumToReduce = 0;
                            } else if (result1 > 0 && result1<deployedServiceNumToReduce) {
                                InstanceDeployOnNode[nodeID][ServiceID] = 0;
                                deployedServiceNumToReduce -= result1;
                            }
                        }
                    } else if (nowServiceInstanceNum == pastServiceInstanceNum) {
                        //对所有服务器节点n，保持微服务m现有的部署方案
                    } else if (nowServiceInstanceNum > pastServiceInstanceNum) {
                        //更新节点利用率
                        Map<Integer,Double> utilizationActive = CalUtilizationActive(InstanceDeployOnNode, appParams.getNum_CPU_Core());//这边索引对应的是节点的id
                        ArrayList<AppPathInfo> currentAppList = alltimeApp.get(time).getAppPathInfos();
                        Map<Integer,Map<Integer, Integer>> backwardServiceInstanceNum = initMap(appParams);
                        Map<Integer,Map<Integer, Integer>> forwardServiceInstanceNum = initMap(appParams);
                        for (AppPathInfo app : currentAppList) {
                            //循环各个app的服务路径，找到微服务m的前继微服务以及后继微服务
                            List<PathProbability> pathProbabilities = app.getPathProbabilities();
                            for (PathProbability pathProbability : pathProbabilities) {
                                List<NodeInfo> nodeInfos = pathProbability.getNodeInfos();
                                for (int index = 0; index < nodeInfos.size(); index++) {
                                    NodeInfo nodeInfo = nodeInfos.get(index);
                                    if (nodeInfo.getServiceType() == ServiceID) {
                                        if (index == 0) {
                                            //如果m是头节点
                                            NodeInfo forwardNode = nodeInfos.get(index + 1);
                                            int forwardSeviceID = forwardNode.getServiceType();//后继微服务的id
                                            int forwardSeviceNum = forwardNode.getInstance_To_Deploy();//后继微服务的实例数
                                            //还需要同步更新这些微服务部署在什么节点上以及具体数量
                                            for(int node = 0; node < appParams.getNum_Server(); node++){
                                                int forwardNodeSeviceNum = forwardNode.getDeployedNode().get(node);//后继微服务在当前物理节点部署的实例数
                                                Map.Entry<Integer, Map<Integer, Integer>> forwardnodeEntry = getEntryByKeyNode(forwardServiceInstanceNum, node);
                                                Map.Entry<Integer, Integer> forwardserviceEntry = getEntryByKey(forwardnodeEntry.getValue(), forwardSeviceID);
                                                forwardserviceEntry.setValue(forwardNodeSeviceNum+forwardserviceEntry.getValue());
                                            }
                                        } else if (index == nodeInfos.size() - 1) {
                                            //如果m是尾节点
                                            NodeInfo backwardNode = nodeInfos.get(index - 1);
                                            int backwardSeviceID = backwardNode.getServiceType();//前继微服务的id
                                            int backwardSeviceNum = backwardNode.getInstance_To_Deploy();//前继微服务的实例数
                                            for(int node = 0; node < appParams.getNum_Server(); node++){
                                                int backwardNodeSeviceNum = backwardNode.getDeployedNode().get(node);//后继微服务在当前物理节点部署的实例数
                                                Map.Entry<Integer, Map<Integer, Integer>> backwardnodeEntry = getEntryByKeyNode(backwardServiceInstanceNum, node);
                                                Map.Entry<Integer, Integer> backwardserviceEntry = getEntryByKey(backwardnodeEntry.getValue(), backwardSeviceID);
                                                backwardserviceEntry.setValue(backwardNodeSeviceNum+backwardserviceEntry.getValue());
                                            }
                                        } else {
                                            //如果m为dag中间节点
                                            NodeInfo forwardNode = nodeInfos.get(index + 1);
                                            NodeInfo backwardNode = nodeInfos.get(index - 1);
                                            int forwardSeviceID = forwardNode.getServiceType();//后继微服务的id
                                            int forwardSeviceNum = forwardNode.getInstance_To_Deploy();//后继微服务的实例数
                                            int backwardSeviceID = backwardNode.getServiceType();//后继微服务的实例数
                                            int backwardSeviceNum = backwardNode.getInstance_To_Deploy();//前继微服务的实例数
                                            for(int node = 0; node < appParams.getNum_Server(); node++){
                                                int forwardNodeSeviceNum = forwardNode.getDeployedNode().get(node);//后继微服务在当前物理节点部署的实例数
                                                int backwardNodeSeviceNum = backwardNode.getDeployedNode().get(node);//前继微服务在当前物理节点部署的实例数
                                                Map.Entry<Integer, Map<Integer, Integer>> forwardnodeEntry = getEntryByKeyNode(forwardServiceInstanceNum, node);
                                                Map.Entry<Integer, Integer> forwardserviceEntry = getEntryByKey(forwardnodeEntry.getValue(), forwardSeviceID);
                                                forwardserviceEntry.setValue(forwardNodeSeviceNum+forwardserviceEntry.getValue());
                                                Map.Entry<Integer, Map<Integer, Integer>> backwardnodeEntry = getEntryByKeyNode(backwardServiceInstanceNum, node);
                                                Map.Entry<Integer, Integer> backwardserviceEntry = getEntryByKey(backwardnodeEntry.getValue(), backwardSeviceID);
                                                backwardserviceEntry.setValue(backwardNodeSeviceNum+backwardserviceEntry.getValue());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //该循环过后两个map分别存储当前处理的微服务m所有前继微服务以及后继微服务在所有物理节点的部署数量
                        //接着可以统计激活节点的前继和后继微服务数量
                        // 计算合计的微服务实例数
                        Map<Integer, Integer> totalServiceInstances = calculateTotalServiceInstances(backwardServiceInstanceNum, forwardServiceInstanceNum);
                        //计算加权和,对服务器节点集合进行降序排序
                        Map<Integer,Double> weightedSum = calculateWeightedSum(utilizationActive,totalServiceInstances,pastServiceInstanceNum);
                        int incrementInstanceNum = nowServiceInstanceNum - pastServiceInstanceNum;
                        while (incrementInstanceNum>0) {
                            int lastNode = 0;
                            for (Map.Entry<Integer, Double> entry : weightedSum.entrySet()) {
                                int nodeID = entry.getKey();
                                int result1 = (int) equalizationCoefficient * appParams.getNum_CPU_Core();//存储每个节点均衡部署的实例数量
                                int result2 = appParams.getNum_CPU_Core();//存储每个节点可部署最多的实例数量
                                int result3 = appParams.getNum_CPU_Core(); //存储在当前时隙该节点可用的资源数量
                                int result4 = result1 - result2 + result3;
                                //计算当前节点可用资源数量
                                for (int service = 0; service < appParams.getNum_Server(); service++) {
                                    result3 -= InstanceDeployOnNode[nodeID][service];
                                }
                                if (result4 > incrementInstanceNum) {
                                    //直接分配实例资源
                                    InstanceDeployOnNode[nodeID][ServiceID] += incrementInstanceNum;
                                    incrementInstanceNum = 0;//被完全部署
                                } else if (result4 > 0 && result4 < incrementInstanceNum) {
                                    InstanceDeployOnNode[nodeID][ServiceID] += result4;
                                    incrementInstanceNum -= result4;//被完全部署
                                } else if (result4 < 0) {
                                    continue;
                                }
                                if (incrementInstanceNum == 0) {
                                    break;
                                }
                                if (incrementInstanceNum > 0) {
                                    lastNode = nodeID;
                                }
                            }
                            //现有的服务器不足够了，这个时候需要增加新的激活服务器来满足计算资源需求
                            if (incrementInstanceNum > 0) {
                                int activateNode = FindNewNodeToActivate(InstanceDeployOnNode,appParams,lastNode);
                                InstanceDeployOnNode[activateNode][ServiceID] += incrementInstanceNum;
                                incrementInstanceNum = 0;//被完全部署
                            }
                        }
                    }
                    alltimeApp.get(time).setInstanceDeployOnNode(InstanceDeployOnNode);
                }
                System.out.println("当前"+time+"时隙部署结果"+Arrays.deepToString(InstanceDeployOnNode));
            }
            //计算当前时隙路由
            //打印部署结果
            System.out.println("当前部署情况X(t):");
            for (int i = 0; i < alltimeApp.get(time).getInstanceDeployOnNode().length; i++) {
                System.out.println(Arrays.toString(alltimeApp.get(time).getInstanceDeployOnNode()[i]));
            }
            //打印当前时隙所有请求流到达率
            for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                System.out.println("APP" + i + "的到达率为:" + alltimeApp.get(time).getAppPathInfos().get(i).getArrivalRate());
            }
            //遍历每条请求流
            for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                System.out.println("APP" + i + " =======");
                int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                Iterator<PathProbability> it_mspath = alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities().iterator();
                while (it_mspath.hasNext()){
                    PathProbability one_mspath = it_mspath.next();
                    System.out.printf("链: ");
                    for (int j = 0; j < one_mspath.getNodeInfos().size(); j++) {
                        System.out.printf("微服务" + one_mspath.getNodeInfos().get(j).getServiceType() + " ");
                    }
                    System.out.println();
                    System.out.println("ArrivateRate:" + one_mspath.getArrivalRate() + "微服务路径概率:" + one_mspath.getProbability());
                    List<List> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                    System.out.println(Routing_tables_eachPath);
                }
            }
            int[][] Routing_decision_Y = alltimeApp.get(time).genRouting_decision_Y(); //决策变量，论文中的Y(t)，其实感觉没啥吊用
            /*for (int i = 0; i < Routing_decision_Y.length; i++) {
                for (int k = 0; k < Routing_decision_Y[0].length; k++) {
                    System.out.print(Routing_decision_Y[i][k]);
                }
                System.out.println();
            }*/

        }
    }


    /**
      * @Description : 寻找满足约束的通信时延最小，带宽最大的节点
      * @Author : Dior
      *  * @param InstanceDeployOnNode
 * @param appParams
 * @param lastNode
      * @return : int
      * @Date : 2024/7/19
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static int FindNewNodeToActivate(int[][] InstanceDeployOnNode,App_Params appParams,int lastNode) {
        Map<Integer,Double> optionalNodes = new HashMap<>();
        for (int node = 0; node < appParams.getNum_Server(); node++) {
            //找到未部署过微服务的节点
            int DeployedNum = 0;
            for (int service = 0; service < appParams.getNum_Microservice(); service++) {
                DeployedNum += InstanceDeployOnNode[node][service];
            }
            if(DeployedNum==0){
                double  comparisonsCoefficient = 0.5*appParams.getPhysicalConnectionDelay()[node][lastNode] +0.5/appParams.getPhysicalConnectionBandwidth()[node][lastNode];
                optionalNodes.put(node,comparisonsCoefficient);
            }
        }
        sortByValueAscendingDouble(optionalNodes);
        int targetNode = optionalNodes.entrySet().iterator().next().getKey();
        return targetNode;
    }

    private static double calculateRedundantFactor (int[][] InstanceDeployOnNode, ArrayList<Integer> nowServiceInstanceNum) {
        double RedundantFactor= 0;
        double molecule = 0;
        double denominator = 0;
        for (int service = 0; service < InstanceDeployOnNode[0].length; service++) {
            int serviceInstanceNum = 0;
            for (int node = 0; node < InstanceDeployOnNode.length; node++) {
                 serviceInstanceNum +=InstanceDeployOnNode[node][service];
            }
            double higherValue = serviceInstanceNum-nowServiceInstanceNum.get(service)>0 ? serviceInstanceNum-nowServiceInstanceNum.get(service) : 0;
            molecule +=  higherValue;
            denominator = higherValue > denominator ? higherValue : denominator;
        }
        RedundantFactor = molecule/InstanceDeployOnNode[0].length/denominator;
        return RedundantFactor;
    }

    /**
      * @Description : 计算加权和
      * @Author : Dior
      *  * @param utilizationActive
 * @param totalServiceInstances
 * @param pastServiceInstanceNum
      * @return : java.util.Map<java.lang.Integer,java.lang.Double>
      * @Date : 2024/7/17
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    private static Map<Integer, Double> calculateWeightedSum(Map<Integer, Double> utilizationActive, Map<Integer, Integer> totalServiceInstances, int pastServiceInstanceNum) {
        int nodeNum = utilizationActive.size();
        Map<Integer, Double> weightedSumNode = new HashMap<>();
        double coefficient1 = 0.5;
        double coefficient2 =0.5;
        for (int node = 0; node < nodeNum; node++) {
            Map.Entry<Integer, Double> utilizationEntry = getEntryByKeyDouble(utilizationActive, node);
            Map.Entry<Integer, Integer> serviceInstancesEntry = getEntryByKey(totalServiceInstances, node);
            if(utilizationEntry.getValue()!=0) {
                double weightedSum = coefficient1 * utilizationEntry.getValue() + coefficient2 * serviceInstancesEntry.getValue() / pastServiceInstanceNum;
                weightedSumNode.put(node, weightedSum);
            }
        }
        sortByValueAscendingDouble(weightedSumNode);
        return weightedSumNode;
    }

    /**
      * @Description : 初始第一次部署，在第一个时隙采用贪心的部署方式，每次只部署一个微服务的实例 如果遇到节点上无法完全部署就拆分
      * @Author : Dior
      *  * @param initDeployMsOnNode
 * @param appParams
 * @param deployServiceID
 * @param deployInstanceNum
      * @return : int[][]
      * @Date : 2024/7/16
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    public static int[][] InitDeployMsOnNode(int[][] initDeployMsOnNode,App_Params appParams,int deployServiceID,int deployInstanceNum) {
        Map<Integer,Integer> freeResourceNode = new HashMap<>();
        for (int node = 0; node < appParams.getNum_Server(); node++) {
            freeResourceNode.put(node,appParams.getNum_CPU_Core());
        }
        for (int node = 0; node < appParams.getNum_Server(); node++) {
            for (int service = 0; service < appParams.getNum_Microservice(); service++) {
                //初始化当前节点剩余资源 freeResourceNode为当前节点剩余资源数量
                int activeResourceOnNode = freeResourceNode.get(node);
                int instanceDeployed = initDeployMsOnNode[node][service];
                if(instanceDeployed > 0){
                    freeResourceNode.replace(node,activeResourceOnNode-initDeployMsOnNode[node][service]);
                }
            }
        }
        //将节点剩余资源按从高到低排序
        //采用贪心的方式将该实例进行部署
        //如果无法完全部署 考虑做拆分 直接拆分 更新节点剩余资源后直接招新节点进行部署
        freeResourceNode = sortByValueDescending(freeResourceNode);
        //此步骤之前适用于所有的时隙间需要增加部署实例的情况
        // 使用增强的 for 循环遍历 Map
        for (Map.Entry<Integer, Integer> entry : freeResourceNode.entrySet()) {
            Integer nodeID = entry.getKey();
            Integer instanceNum = entry.getValue();
            if( instanceNum >= deployInstanceNum){
                //如果节点资源满足微服务部署要求
                initDeployMsOnNode[nodeID][deployServiceID] = deployInstanceNum;
                freeResourceNode.replace(nodeID,instanceNum-deployInstanceNum);
                break;
            }else {
                int instanceDeployOnCurrentNode = instanceNum;
                initDeployMsOnNode[nodeID][deployServiceID] = instanceNum;
                deployInstanceNum -= instanceDeployOnCurrentNode;
                freeResourceNode.replace(nodeID,0);
            }
        }
        return initDeployMsOnNode;
    }
    /**
      * @Description : 计算当前激活节点上所有存在于P_m和S_m的微服务的实例的总数量
      * @Author : Dior
      *  * @param backwardServiceInstanceNum
 * @param forwardServiceInstanceNum
      * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/7/17 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    public static Map<Integer, Integer> calculateTotalServiceInstances(Map<Integer, Map<Integer, Integer>> backwardServiceInstanceNum, Map<Integer, Map<Integer, Integer>> forwardServiceInstanceNum) {
        Map<Integer, Integer> totalServiceInstances = new HashMap<>();
        // 合计backwardServiceInstanceNum中的实例数
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : backwardServiceInstanceNum.entrySet()) {
            int nodeId = entry.getKey();
            Map<Integer, Integer> services = entry.getValue();
            int totalInstances = services.values().stream().mapToInt(Integer::intValue).sum();
            totalServiceInstances.put(nodeId, totalInstances);
        }

        // 合计forwardServiceInstanceNum中的实例数
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : forwardServiceInstanceNum.entrySet()) {
            int nodeId = entry.getKey();
            Map<Integer, Integer> services = entry.getValue();
            int totalInstances = services.values().stream().mapToInt(Integer::intValue).sum();
            totalServiceInstances.merge(nodeId, totalInstances, Integer::sum);
        }
        return totalServiceInstances;
    }

    /**
      * @Description : 初始化map 便于后续进行内容替换
      * @Author : Dior
      *  * @param appParams
      * @return : java.util.Map<java.lang.Integer,java.util.Map<java.lang.Integer,java.lang.Integer>>
      * @Date : 2024/7/17 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    public static Map<Integer,Map<Integer, Integer>> initMap(App_Params appParams) {
        Map<Integer,Map<Integer, Integer>> ServiceInstance = new HashMap<>();
        for (int node = 0; node < appParams.getNum_Server(); node++){
            Map<Integer, Integer> ServiceOnNode = new HashMap<>();
            for (int service = 0; service < appParams.getNum_Microservice(); service++){
                ServiceOnNode.put(service,0);
            }
            ServiceInstance.put(node,ServiceOnNode);
        }
        return ServiceInstance;
    }


    /**
      * @Description : 找到指定key值的键值对
      * @Author : Dior
      *  * @param map
 * @param key
      * @return : java.util.Map.Entry<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/7/15
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map.Entry<Integer, Integer> getEntryByKey(Map<Integer, Integer> map, int key) {
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    public static Map.Entry<Integer, Double> getEntryByKeyDouble(Map<Integer, Double> map, int key) {
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    public static Map.Entry<Integer, Map<Integer, Integer>> getEntryByKeyNode(Map<Integer, Map<Integer, Integer>> map, int key) {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    /**
      * @Description : 将当前微服务的存储结果转换为map形式
      * @Author : Dior
      *  * @param serviceInstanceNum
      * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/7/15
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map<Integer,Integer> ArrayToSortedMap(ArrayList<Integer> serviceInstanceNum) {
        Map<Integer,Integer> sortedServiceInstanceNum = new HashMap<>();
        for (int serviceID = 0; serviceID < serviceInstanceNum.size(); serviceID++){
            sortedServiceInstanceNum.put(serviceID,serviceInstanceNum.get(serviceID));
        }
        sortByValueAscending(sortedServiceInstanceNum);
        return sortedServiceInstanceNum;
    }
    /**
      * @Description : 将当前map按照value值升序排序
      * @Author : Dior
      *  * @param map
      * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/7/15
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map<Integer, Integer> sortByValueAscending(Map<Integer, Integer> map) {
        // 将Map的Entry放入List
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());

        // 按值进行升序排序
        list.sort((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()));

        // 创建一个有序的LinkedHashMap
        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    /**
     * @Description : 将当前map按照value值降序排序
     * @Author : Dior
     *  * @param map
     * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
     * @Date : 2024/7/15
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<Integer, Integer> sortByValueDescending(Map<Integer, Integer> map) {
        // 将Map的Entry放入List
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());

        // 按值进行排序
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        // 创建一个有序的LinkedHashMap
        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    /**
      * @Description : 将当前map按照value值升序排序
      * @Author : Dior
      *  * @param map
      * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/7/17
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map<Integer, Double> sortByValueAscendingDouble(Map<Integer, Double> map) {
        // 将Map的Entry放入List
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());

        // 按值进行升序排序
        list.sort((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()));

        // 创建一个有序的LinkedHashMap
        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    /**
      * @Description : 获取各个节点的平均利用率
      * @Author : Dior
      *  * @param instanceDeployOnNode
 * @param nodeResource
      * @return : java.util.ArrayList<java.lang.Double>
      * @Date : 2024/7/15
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map<Integer,Double> CalUtilizationActive(int[][] instanceDeployOnNode,int nodeResource) {
        Map<Integer,Double> avgUilizationActive = new HashMap<>();
        for(int server = 0; server < instanceDeployOnNode.length; server++){
            int instanceOnCurrentNode = 0;
            for(int service = 0; service < instanceDeployOnNode[0].length; service++){
                instanceOnCurrentNode += instanceDeployOnNode[server][service];
            }
            double currentNodeUtilization = (double) instanceOnCurrentNode/nodeResource;
            avgUilizationActive.put(server,currentNodeUtilization);
        }
        return avgUilizationActive;
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
