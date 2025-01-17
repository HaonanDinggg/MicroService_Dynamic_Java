package utils;

import java.sql.SQLOutput;
import java.util.*;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-24 20:23
 **/
public class InstanceDeploy {
    public static ArrayList<CurrentTimeApps> DeployInstance(App_Params appParams, ArrayList<CurrentTimeApps> alltimeApp) {
        //准备部署实例
        for(int time = 0; time < alltimeApp.size(); time++){
            double equalizationCoefficient = appParams.getEqualizationCoefficient();//公平指数下限
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
                    InitDeployMsOnNode(alltimeApp.get(time).getInstanceDeployOnNode(),appParams,ServiceID,incrementInstanceNum,equalizationCoefficient);
                }
                System.out.println("当前"+(time+1)+"时隙部署结果"+ Arrays.deepToString(InstanceDeployOnNode));
                System.out.println();
            }else {
                System.out.println("进行第"+(time+1)+"个时隙的实例部署");
                PastServiceInstanceNum = alltimeApp.get(time-1).getServiceInstanceNum();//获取前一个时隙后的app情况
                System.out.println("当前部署数量"+alltimeApp.get(time).getServiceInstanceNum());
                //在第一个时隙初始化部署矩阵
                int[][] InstanceDeployOnNode = deepCopy(alltimeApp.get(time-1).getInstanceDeployOnNode());
                alltimeApp.get(time).setInstanceDeployOnNode(InstanceDeployOnNode);

                double redundantFactor = calculateRedundantFactor(InstanceDeployOnNode,NowServiceInstanceNum);//当前时隙的冗余因子
                System.out.println("打印冗余因子" + redundantFactor);
                //将该时隙微服务数量按升序排序
                Map<Integer,Integer> sortedNowServiceInstanceNum = ArrayToSortedMap(NowServiceInstanceNum);
                Map<Integer,Integer> sortedPastServiceInstanceNum = ArrayToSortedMap(PastServiceInstanceNum);
                // 打印排序后的结果
                for (Map.Entry<Integer, Integer> nowEntry : sortedNowServiceInstanceNum.entrySet()) {
                    int ServiceID = nowEntry.getKey();
                    int nowServiceInstanceNum = nowEntry.getValue();
                    // 获取指定键的Entry
                    Map.Entry<Integer, Integer> PastEntry = getEntryByKey(sortedPastServiceInstanceNum, ServiceID);
                    if(nowEntry.getValue() == 0){
                        //如果该时隙下不存在该微服务的实例 回收微服务m的所有实例
                        for(int node = 0; node < appParams.getNum_Server(); node++){
                            InstanceDeployOnNode[node][ServiceID] = 0;
                        }
                        continue;
                    }
                    int pastDeployedServiceNum = 0;
                    for(int node = 0; node < appParams.getNum_Server(); node++){
                        pastDeployedServiceNum += InstanceDeployOnNode[node][ServiceID];
                    }
                    if(nowServiceInstanceNum < pastDeployedServiceNum){
                        Map<Integer,Integer> serviceDeloyedNum = new HashMap<>();
                        int actualDeployedServiceNum = nowServiceInstanceNum + (int) redundantFactor*Math.abs(nowServiceInstanceNum-pastDeployedServiceNum);//实际需要部署的实例
                        int deployedServiceNumToReduce = pastDeployedServiceNum - actualDeployedServiceNum;
                        for(int node = 0; node < appParams.getNum_Server(); node++){
                            serviceDeloyedNum.put(node,InstanceDeployOnNode[node][ServiceID]);
                        }
                        sortByValueAscending(serviceDeloyedNum);
                        for (Map.Entry<Integer, Integer> entry : serviceDeloyedNum.entrySet()) {
                            //遍历所有包含m的节点 升序排序，先清除小的
                            int nodeID = entry.getKey();
                            int result1 = InstanceDeployOnNode[nodeID][ServiceID]; //存储在当前时隙该节点可减少的资源数量
                            if(result1 > deployedServiceNumToReduce){
                                InstanceDeployOnNode[nodeID][ServiceID] -= deployedServiceNumToReduce;
                                deployedServiceNumToReduce = 0;
                            } else if ( result1<deployedServiceNumToReduce) {
                                InstanceDeployOnNode[nodeID][ServiceID] = 0;
                                deployedServiceNumToReduce -= result1;
                            }
                        }
                    } else if (nowServiceInstanceNum == pastDeployedServiceNum) {
                        //对所有服务器节点n，保持微服务m现有的部署方案
                    } else if (nowServiceInstanceNum > pastDeployedServiceNum) {

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
                        Map<Integer,Double> weightedSum = calculateWeightedSum(utilizationActive,totalServiceInstances,pastDeployedServiceNum);
                        int incrementInstanceNum = nowServiceInstanceNum - pastDeployedServiceNum;
                        while (incrementInstanceNum > 0) {
                            int lastNode = 0;
                            for (Map.Entry<Integer, Double> entry : weightedSum.entrySet()) {
                                int nodeID = entry.getKey();
                                int result1 = (int) (equalizationCoefficient * appParams.getNum_CPU_Core());//存储每个节点均衡部署的实例数量
                                int result2 = appParams.getNum_CPU_Core();//存储每个节点可部署最多的实例数量
                                int result3 = appParams.getNum_CPU_Core(); //存储在当前时隙该节点可用的资源数量
                                //计算当前节点可用资源数量
                                for (int service = 0; service < appParams.getNum_Microservice(); service++) {
                                    result3 -= InstanceDeployOnNode[nodeID][service];
                                }
                                int result4 = result1 - result2 + result3;
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
                                int result1 = (int) (equalizationCoefficient * appParams.getNum_CPU_Core());//存储每个节点均衡部署的实例数量
                                int result2 = appParams.getNum_CPU_Core();//存储每个节点可部署最多的实例数量
                                int result3 = appParams.getNum_CPU_Core(); //存储在当前时隙该节点可用的资源数量
                                for (int service = 0; service < appParams.getNum_Microservice(); service++) {
                                    result3 -= InstanceDeployOnNode[activateNode][service];
                                }
                                int result4 = result1 - result2 + result3;
                                if (result4 > incrementInstanceNum) {
                                    //直接分配实例资源
                                    InstanceDeployOnNode[activateNode][ServiceID] += incrementInstanceNum;
                                    incrementInstanceNum = 0;//被完全部署
                                } else {
                                    InstanceDeployOnNode[activateNode][ServiceID] += result4;
                                    incrementInstanceNum -= result4;//被完全部署
                                }
                            }
                        }
                    }
                }
                System.out.println("当前第"+(time+1)+"个时隙部署结果"+Arrays.deepToString(InstanceDeployOnNode));
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
            System.out.println("计算路由");
            //遍历每条请求流
            for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                for (PathProbability one_mspath : alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities()) {
                    List<List<List<Object>>> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                }
            }
            int[][] Routing_decision_Y = alltimeApp.get(time).genRouting_decision_Y(); //决策变量，论文中的Y(t)，其实感觉没啥吊用
            alltimeApp.get(time).genBandwidthResourceAndArrivalmatrix(appParams);
            double[][] BandwidthResource = alltimeApp.get(time).getBandwidthResource();
            double[][] Arrival_matrix = alltimeApp.get(time).getArrivalRate_matrix();
            //打印当前时隙请求率
            double arrival_sum = 0;
            for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                arrival_sum += alltimeApp.get(time).getAppPathInfos().get(i).getArrivalRate();
            }
            System.out.println(arrival_sum);
            System.out.println("当前时隙到达率矩阵:");
            for (int i = 0; i < Arrival_matrix.length; i++) {
                System.out.println(Arrays.toString(Arrival_matrix[i]));
            }
        }
        return alltimeApp;
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

    /**
     * @Description : 计算加权和1
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
        double coefficient1 = 0.9;
        double coefficient2 =0.1;
        for (int node = 0; node < nodeNum; node++) {
            Map.Entry<Integer, Double> utilizationEntry = getEntryByKeyDouble(utilizationActive, node);
            Map.Entry<Integer, Integer> serviceInstancesEntry = getEntryByKey(totalServiceInstances, node);
            if(utilizationEntry.getValue()!=0&&pastServiceInstanceNum>0) {
                double weightedSum = coefficient1 * utilizationEntry.getValue() + coefficient2 * serviceInstancesEntry.getValue() / pastServiceInstanceNum;
                weightedSumNode.put(node, weightedSum);
            }else {
                double weightedSum = utilizationEntry.getValue();
                weightedSumNode.put(node, weightedSum);
            }
        }
        sortByValueAscendingDouble(weightedSumNode);
        return weightedSumNode;
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
    public static int[][] InitDeployMsOnNode(int[][] initDeployMsOnNode,App_Params appParams,int deployServiceID,int deployInstanceNum,double equalizationCoefficient ) {
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
        freeResourceNode = sortByValueAscending(freeResourceNode);
        //此步骤之前适用于所有的时隙间需要增加部署实例的情况
        // 使用增强的 for 循环遍历 Map
        for (Map.Entry<Integer, Integer> entry : freeResourceNode.entrySet()) {
            Integer nodeID = entry.getKey();
            Integer instanceNum = entry.getValue();
            int result1 = (int) (equalizationCoefficient * appParams.getNum_CPU_Core());//存储每个节点均衡部署的实例数量
            int result2 = appParams.getNum_CPU_Core();//存储每个节点可部署最多的实例数量
            int result3 = appParams.getNum_CPU_Core(); //存储在当前时隙该节点可用的资源数量
            //计算当前节点可用资源数量
            for (int service = 0; service < appParams.getNum_Microservice(); service++) {
                result3 -= initDeployMsOnNode[nodeID][service];
            }
            int result4 = result1 - result2 + result3;
            if( deployInstanceNum <= result4 ){
                //如果节点资源满足微服务部署要求
                initDeployMsOnNode[nodeID][deployServiceID] += deployInstanceNum;
                deployInstanceNum = 0;
                freeResourceNode.replace(nodeID,0);

                break;
            }else if (result4 > 0 && result4 < deployInstanceNum) {
                initDeployMsOnNode[nodeID][deployServiceID] += result4;
                deployInstanceNum -= result4;
                freeResourceNode.replace(nodeID,instanceNum-deployInstanceNum);
            }
        }
        return initDeployMsOnNode;
    }

    // 深拷贝方法
    public static int[][] deepCopy(int[][] original) {
        if (original == null) {
            return null;
        }

        int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = original[i].clone();
        }
        return result;
    }

    /**
     * @Description : 当前时隙的冗余因子
     * @Author : Dior
     *  * @param InstanceDeployOnNode
     * @param nowServiceInstanceNum
     * @return : double
     * @Date : 2024/7/23
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
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
        if(denominator !=0){
            RedundantFactor = molecule/InstanceDeployOnNode[0].length/denominator;
        }else {
            RedundantFactor = 0;
        }
        return RedundantFactor;
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


    public static Map.Entry<Integer, Double> getEntryByKeyDouble(Map<Integer, Double> map, int key) {
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    /**
      * @Description : 根据key的node值获取entry
      * @Author : Dior
      *  * @param map
 * @param key
      * @return : java.util.Map.Entry<java.lang.Integer,java.util.Map<java.lang.Integer,java.lang.Integer>>
      * @Date : 2024/7/24
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.
      **/
    public static Map.Entry<Integer, Map<Integer, Integer>> getEntryByKeyNode(Map<Integer, Map<Integer, Integer>> map, int key) {
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : map.entrySet()) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
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

}
