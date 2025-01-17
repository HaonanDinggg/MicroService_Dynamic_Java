package utils;

import java.util.*;
import java.util.stream.Collectors;

import static utils.WaitingTime.calculateMicroserviceNodeAverageServiceTime;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-26 10:55
 **/
public class Migration {
    public static ArrayList<CurrentTimeApps> Migration(App_Params appParams, ArrayList<CurrentTimeApps> alltimeApp) {
        for(int time = 0; time < alltimeApp.size(); time++){
            System.out.println("准备进行第"+(time+1)+"个时隙的迁移");
            double[][] bandwidthResource = alltimeApp.get(time).getBandwidthResource();
            Map<NodePair, Double> bandwidthMap = sortBandwidthResource(bandwidthResource);
            int[][] instanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
            Map<Integer,Double> utilizationActive = CalUtilizationActive(instanceDeployOnNode, appParams.getNum_CPU_Core());//这边索引对应的是节点的id
            double avgNetworkResourceUtilization = appParams.getAvgNetworkResourceUtilization();//平均网络资源利用率上限
            List<Double> migrationCost = new ArrayList<>();
            for (int i = 0; i < appParams.getNum_Microservice(); i++) {
                migrationCost.add(0.0); // 或者任何其他默认值
            }
            for (Map.Entry<NodePair, Double> entry : bandwidthMap.entrySet()) {
//                System.out.println("处理当前连接");
                NodePair nodePair  = entry.getKey();
                double accessibleBandwidth = entry.getValue();
                if(accessibleBandwidth<0){
                    List<List<Object>> topologyPairs = new ArrayList<>();
                    ArrayList<AppPathInfo> appPathInfos = alltimeApp.get(time).getAppPathInfos();
                    for (AppPathInfo appPathInfo : appPathInfos) {
                        List<PathProbability> pathProbabilities = appPathInfo.getPathProbabilities();
                        for (PathProbability pathProbability : pathProbabilities) {
                            List<List<List<Object>>>routing_tables_eachPath = pathProbability.getRouting_tables_eachPath();
                            for (List<List<Object>> microService : routing_tables_eachPath) {
                                //这层循环遍历该路径所有微服务
                                for (List<Object> transRoute : microService) {
                                    int startService = (int) transRoute.get(0);
                                    int startNode = (int) transRoute.get(1);
                                    int endService = (int) transRoute.get(2);
                                    int endNode = (int) transRoute.get(3);
                                    double transpro = (double) transRoute.get(4);
                                    List<Object> transferableTopologyPair = new ArrayList<>();
                                    int bandNode1 =  nodePair.getNode1();
                                    int bandNode2 =  nodePair.getNode2();
                                    if(startNode == bandNode1){
                                        if(endNode == bandNode2){
                                            transferableTopologyPair.add(startService);
                                            transferableTopologyPair.add(startNode);
                                            transferableTopologyPair.add(endService);
                                            transferableTopologyPair.add(endNode);
                                            transferableTopologyPair.add(transpro*pathProbability.getArrivalRate());
                                            topologyPairs.add(transferableTopologyPair);
                                        }
                                    }
                                    if(startNode == bandNode2){
                                        if(endNode == bandNode1){
                                            transferableTopologyPair.add(startService);
                                            transferableTopologyPair.add(startNode);
                                            transferableTopologyPair.add(endService);
                                            transferableTopologyPair.add(endNode);
                                            transferableTopologyPair.add(transpro*pathProbability.getArrivalRate());
                                            topologyPairs.add(transferableTopologyPair);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    topologyPairs = MergeTopologyPairs(topologyPairs);
                    // 将topologyPairs根据里面List<Object>的第五个元素数据通信量进行升序排序
                    Collections.sort(topologyPairs, new Comparator<List<Object>>() {
                        @Override
                        public int compare(List<Object> o1, List<Object> o2) {
                            Double value1 = (Double) o1.get(4);
                            Double value2 = (Double) o2.get(4);
                            return value1.compareTo(value2);
                        }
                    });
                    double bandwidthToTighten = 0-accessibleBandwidth;//为正，即需要迁移走的数据量
                    // 输出排序后的结果
//                    for (List<Object> list : topologyPairs) {
//                        System.out.println(list);
//                    }
                    //初始化迁移列表
                    List<List<Integer>> migrationList = new ArrayList<>();
                    for (List<Object> topologyPair : topologyPairs) {
                        int instanceToTighten = 0;
                        int startService = (int) topologyPair.get(0);
                        int startNode = (int) topologyPair.get(1);
                        int initStartServiceOnStartNode = instanceDeployOnNode[startNode][startService];
                        int endService = (int) topologyPair.get(2);
                        int endNode = (int) topologyPair.get(3);
                        double dataTraffic = (double) topologyPair.get(4);
                        List<Integer> migrationPair = new ArrayList<>();
                        while(bandwidthToTighten > 0){
                            instanceToTighten += 1;
                            //计算出产生变化的数据通信量
                            double dataTrafficToTighten = (instanceToTighten/initStartServiceOnStartNode*dataTraffic);
                            if(dataTrafficToTighten > bandwidthToTighten || instanceToTighten == initStartServiceOnStartNode ){ //|| instanceToTighten == avgNetworkResourceUtilization*appParams.getNum_CPU_Core()
                                //将迁移对象加入到迁移列表
                                migrationPair.add(startService);
                                migrationPair.add(startNode);
                                migrationPair.add(instanceToTighten);
                                //更新残余带宽
                                bandwidthToTighten -= dataTrafficToTighten;
                                migrationList.add(migrationPair);
                                break;
                            }
                        }
                    }
                    for (List<Integer> list : migrationList) {
                        System.out.println(list);
                    }

                    for (List<Integer> topologyPair : migrationList) {
                        int migrationService = topologyPair.get(0);//从该微服务转出
                        int migrationNode = topologyPair.get(1);//从该节点转出
                        int instanceToTighten = topologyPair.get(2);//从该节点的该微服务转出的实例数量
                        if(instanceDeployOnNode[migrationNode][migrationService] == 0){
                            continue;
                        }
                        CurrentTimeApps currentTimeApps = alltimeApp.get(time);
                        ArrayList<AppPathInfo> currentAppList = alltimeApp.get(time).getAppPathInfos();
                        Map<Integer,Integer> backwardServiceInstanceNum = initMapInt(appParams);
                        Map<Integer,Integer> forwardServiceInstanceNum = initMapInt(appParams);
                        Map<Integer,Double> backwardServiceArrivalRate = initMapDouble(appParams);
                        Map<Integer,Double> forwardServiceArrivalRate = initMapDouble(appParams);
                        for (AppPathInfo app : currentAppList) {
                            //循环各个app的服务路径，找到微服务m的前继微服务以及后继微服务
                            List<PathProbability> pathProbabilities = app.getPathProbabilities();
                            for (PathProbability pathProbability : pathProbabilities) {
                                List<NodeInfo> nodeInfos = pathProbability.getNodeInfos();
                                List<List<List<Object>>>routing_tables_eachPath = pathProbability.getRouting_tables_eachPath();
                                for (int index = 0; index < nodeInfos.size(); index++) {
                                    NodeInfo nodeInfo = nodeInfos.get(index);
                                    if (nodeInfo.getServiceType() == migrationService) {
                                        if (index == 0) {
                                            //如果m是头节点
                                            NodeInfo forwardNode = nodeInfos.get(index + 1);
                                            int forwardSeviceID = forwardNode.getServiceType();//后继微服务的id
                                            for (List<List<Object>> microService : routing_tables_eachPath) {
                                                //这层循环遍历该路径所有微服务
                                                for (List<Object> transRoute : microService) {
                                                    int startService = (int) transRoute.get(0);
                                                    int startNode = (int) transRoute.get(1);
                                                    int endService = (int) transRoute.get(2);
                                                    int endNode = (int) transRoute.get(3);
                                                    double transpro = (double) transRoute.get(4);
                                                    if(startNode == migrationNode && startService == migrationService && endService == forwardSeviceID){
                                                        //此时找到后继微服务 在endNode上的endService
                                                        int forwardSeviceOnNode = instanceDeployOnNode[endNode][endService];
                                                        double forwardSeviceArrivaRateOnNode = transpro*pathProbability.getArrivalRate();
                                                        Map.Entry<Integer, Integer> forwardserviceEntry = getEntryByKey(forwardServiceInstanceNum, endNode);
                                                        Map.Entry<Integer, Double> forwardserviceArrivalRateEntry = getEntryByKeyDouble(forwardServiceArrivalRate, endNode);
                                                        forwardServiceInstanceNum.replace(endNode,forwardserviceEntry.getValue()+forwardSeviceOnNode);
                                                        forwardServiceArrivalRate.replace(endNode,forwardserviceArrivalRateEntry.getValue()+forwardSeviceArrivaRateOnNode);
                                                    }
                                                }
                                            }

                                        } else if (index == nodeInfos.size() - 1) {
                                            //如果m是尾节点
                                            NodeInfo backwardNode = nodeInfos.get(index - 1);
                                            int backwardSeviceID = backwardNode.getServiceType();//前继微服务的id
                                            for (List<List<Object>> microService : routing_tables_eachPath) {
                                                //这层循环遍历该路径所有微服务
                                                for (List<Object> transRoute : microService) {
                                                    int startService = (int) transRoute.get(0);
                                                    int startNode = (int) transRoute.get(1);
                                                    int endService = (int) transRoute.get(2);
                                                    int endNode = (int) transRoute.get(3);
                                                    double transpro = (double) transRoute.get(4);
                                                    if(endNode == migrationNode && endService == migrationService && startService == backwardSeviceID){
                                                        //此时找到前继微服务 在startNode上的startService
                                                        int backwardSeviceOnNode = instanceDeployOnNode[startNode][startService];
                                                        double backwardSeviceArrivaRateOnNode = transpro*pathProbability.getArrivalRate();
                                                        Map.Entry<Integer, Integer> backwardserviceEntry = getEntryByKey(backwardServiceInstanceNum, startNode);
                                                        Map.Entry<Integer, Double> backwardserviceArrivalRateEntry = getEntryByKeyDouble(backwardServiceArrivalRate, startNode);
                                                        backwardServiceInstanceNum.replace(startNode,backwardserviceEntry.getValue()+backwardSeviceOnNode);
                                                        backwardServiceArrivalRate.replace(startNode,backwardserviceArrivalRateEntry.getValue()+backwardSeviceArrivaRateOnNode);
                                                    }
                                                }
                                            }
                                        } else {
                                            //如果m为dag中间节点
                                            NodeInfo forwardNode = nodeInfos.get(index + 1);
                                            NodeInfo backwardNode = nodeInfos.get(index - 1);
                                            int forwardSeviceID = forwardNode.getServiceType();//后继微服务的id
                                            int backwardSeviceID = backwardNode.getServiceType();//后继微服务的实例数
                                            for (List<List<Object>> microService : routing_tables_eachPath) {
                                                //这层循环遍历该路径所有微服务
                                                for (List<Object> transRoute : microService) {
                                                    int startService = (int) transRoute.get(0);
                                                    int startNode = (int) transRoute.get(1);
                                                    int endService = (int) transRoute.get(2);
                                                    int endNode = (int) transRoute.get(3);
                                                    double transpro = (double) transRoute.get(4);
                                                    if(endNode == migrationNode && endService == migrationService && startService == backwardSeviceID){
                                                        //此时找到前继微服务
                                                        int backwardSeviceOnNode = instanceDeployOnNode[startNode][startService];
                                                        double backwardSeviceArrivaRateOnNode = transpro*pathProbability.getArrivalRate();
                                                        Map.Entry<Integer, Integer> backwardserviceEntry = getEntryByKey(backwardServiceInstanceNum, startNode);
                                                        Map.Entry<Integer, Double> backwardserviceArrivalRateEntry = getEntryByKeyDouble(backwardServiceArrivalRate, startNode);
                                                        backwardServiceInstanceNum.replace(startNode,backwardserviceEntry.getValue()+backwardSeviceOnNode);
                                                        backwardServiceArrivalRate.replace(startNode,backwardserviceArrivalRateEntry.getValue()+backwardSeviceArrivaRateOnNode);
                                                    }
                                                    if(startNode == migrationNode && startService == migrationService && endService == forwardSeviceID){
                                                        //此时找到后继微服务
                                                        int forwardSeviceOnNode = instanceDeployOnNode[endNode][endService];
                                                        double forwardSeviceArrivaRateOnNode = transpro*pathProbability.getArrivalRate();
                                                        Map.Entry<Integer, Integer> forwardserviceEntry = getEntryByKey(forwardServiceInstanceNum, endNode);
                                                        Map.Entry<Integer, Double> forwardserviceArrivalRateEntry = getEntryByKeyDouble(forwardServiceArrivalRate, endNode);
                                                        forwardServiceInstanceNum.replace(endNode,forwardserviceEntry.getValue()+forwardSeviceOnNode);
                                                        forwardServiceArrivalRate.replace(endNode,forwardserviceArrivalRateEntry.getValue()+forwardSeviceArrivaRateOnNode);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 合并两个Map
                        Map<Integer, Integer> totalServiceInstances = mergeMaps(backwardServiceInstanceNum, forwardServiceInstanceNum);
                        //计算加权和,对服务器节点集合进行降序排序
                        Map<Integer,Double> weightedSum = calculateWeightedSumMigration(utilizationActive,totalServiceInstances,migrationNode,appParams.getPhysicalConnectionDelay());

                        while(instanceToTighten > 0){
                            //System.out.println("需要进行迁移");
                            //初始化每个节点的最大可迁移数量和目的服务器节点列表Migration_Destination_List
                            Map<Integer,Integer> migrationDestinationList = new HashMap<>();
                            for (Map.Entry<Integer, Double> nodeAccessibleEntry : weightedSum.entrySet()) {
                                int nodeID = nodeAccessibleEntry.getKey();
                                int instanceMigration = 0;
                                int nodeResource = appParams.getNum_CPU_Core();
                                while (true){
                                    int nodeAccessibleResource = CalNodeAccessibleResource(instanceDeployOnNode,nodeResource,nodeID,appParams.getNum_Microservice());
                                    if(instanceMigration < (int) (avgNetworkResourceUtilization * nodeResource) - nodeResource + nodeAccessibleResource ){
                                        instanceMigration++;
                                    }else {
                                        if(instanceMigration>0){
                                            migrationDestinationList.put(nodeID,instanceMigration);
                                            System.out.println("当前可供迁移的实例数"+instanceMigration);
                                            break;
                                        }else {
                                            break;
                                        }
                                    }
                                    //计算数据通信量变化 即需要迁移的服务占比
                                    //需要遍历所有的节点 看是否满足约束
                                    boolean flag = false;
                                    for(int migratenode = 0;migratenode < appParams.getNum_Server();migratenode ++) {
                                        double arrivalRate = 0;
                                        arrivalRate += getEntryByKeyDouble(backwardServiceArrivalRate, migratenode).getValue() * instanceToTighten / instanceDeployOnNode[migrationNode][migrationService];
                                        arrivalRate += getEntryByKeyDouble(forwardServiceArrivalRate, migratenode).getValue() * instanceToTighten / instanceDeployOnNode[migrationNode][migrationService];
                                        if(bandwidthResource[nodeID][migratenode] - arrivalRate < 0 || instanceMigration > nodeAccessibleResource || instanceMigration > instanceToTighten){
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if(flag){
                                        instanceMigration--;
                                        if(instanceMigration>0){
                                            migrationDestinationList.put(nodeID,instanceMigration);
                                            System.out.println("当前可供迁移的实例数"+instanceMigration);
                                        }
                                        break;
                                    }
                                }
                            }
                            Map<Integer,List<Object>> decreaseMigrationDestinationList = new HashMap<>();
                            for (Map.Entry<Integer,Integer> migrationDestinationEntry : migrationDestinationList.entrySet()) {
                                int migrationDestinationNode = migrationDestinationEntry.getKey();//转入的节点id
                                int migrationInstanceNum = migrationDestinationEntry.getValue();//转入的实例数量
                                double gain = 0;
                                List<Object> migrationInfo = new ArrayList<>();//该列表第一个元素为迁移实例数量，第二个为迁移增益
                                //计算时延增益
                                int migrationServiceProcessingRate = appParams.getServiceTypeInfos().get(migrationService).getServiceProcessingRate();
                                double procession_delay, trans_delay, physical_delay, total_delay;
                                double procession_delay_migration, trans_delay_migration, physical_delay_migration, total_delay_migration;
                                procession_delay = calculateMicroserviceNodeAverageServiceTime(currentTimeApps.getArrivalRate_matrix()[migrationNode][migrationService],currentTimeApps.getInstanceDeployOnNode()[migrationNode][migrationService],migrationServiceProcessingRate)
                                        + calculateMicroserviceNodeAverageServiceTime(currentTimeApps.getArrivalRate_matrix()[migrationDestinationNode][migrationService],currentTimeApps.getInstanceDeployOnNode()[migrationDestinationNode][migrationService],migrationServiceProcessingRate);
                                trans_delay = currentTimeApps.getDataTrans_NodeToNode()[migrationNode][migrationDestinationNode]/currentTimeApps.getBandwidthResource()[migrationNode][migrationDestinationNode];
                                physical_delay = appParams.getPhysicalConnectionDelay()[migrationNode][migrationDestinationNode];
                                total_delay = procession_delay + trans_delay + physical_delay;
                                System.out.println("procession" + procession_delay);
                                System.out.println("trans_delay" + trans_delay);
                                System.out.println("physical_delay" + physical_delay);
                                int e = Math.min(migrationInstanceNum, Math.min(currentTimeApps.getInstanceDeployOnNode()[migrationNode][migrationService], instanceToTighten));
                                System.out.println("e:" + e);
//                                System.out.println("迁移前X(t)：");
//                                for (int i = 0; i < currentTimeApps.getInstanceDeployOnNode().length; i++) {
//                                    System.out.println(Arrays.toString(currentTimeApps.getInstanceDeployOnNode()[i]));
//                                }
                                //迁移
                                currentTimeApps.getInstanceDeployOnNode()[migrationNode][migrationService] -= e;
                                currentTimeApps.getInstanceDeployOnNode()[migrationDestinationNode][migrationService] += e;
                                //重定向路由
                                for (int i = 0; i < currentTimeApps.getAppPathInfos().size(); i++) {
                                    int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                                    for (PathProbability one_mspath : alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities()) {
                                        List<List<List<Object>>> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                                    }
                                }
                                currentTimeApps.genBandwidthResourceAndArrivalmatrix(appParams);
//                                System.out.println("还原前X(t)：");
//                                for (int i = 0; i < currentTimeApps.getInstanceDeployOnNode().length; i++) {
//                                    System.out.println(Arrays.toString(currentTimeApps.getInstanceDeployOnNode()[i]));
//                                }
                                procession_delay_migration = calculateMicroserviceNodeAverageServiceTime(currentTimeApps.getArrivalRate_matrix()[migrationNode][migrationService],currentTimeApps.getInstanceDeployOnNode()[migrationNode][migrationService],migrationServiceProcessingRate)
                                        + calculateMicroserviceNodeAverageServiceTime(currentTimeApps.getArrivalRate_matrix()[migrationDestinationNode][migrationService],currentTimeApps.getInstanceDeployOnNode()[migrationDestinationNode][migrationService],migrationServiceProcessingRate);
                                trans_delay_migration = currentTimeApps.getDataTrans_NodeToNode()[migrationNode][migrationDestinationNode]/currentTimeApps.getBandwidthResource()[migrationNode][migrationDestinationNode];
                                physical_delay_migration = appParams.getPhysicalConnectionDelay()[migrationNode][migrationDestinationNode];
                                total_delay_migration = procession_delay_migration + trans_delay_migration + physical_delay_migration;

                                //还原
                                currentTimeApps.getInstanceDeployOnNode()[migrationNode][migrationService] += e;
                                currentTimeApps.getInstanceDeployOnNode()[migrationDestinationNode][migrationService] -= e;
                                //重定向路由
                                for (int i = 0; i < currentTimeApps.getAppPathInfos().size(); i++) {
                                    int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                                    for (PathProbability one_mspath : alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities()) {
                                        List<List<List<Object>>> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                                    }
                                }
                                currentTimeApps.genBandwidthResourceAndArrivalmatrix(appParams);
//                                System.out.println("还原后X(t)：");
//                                for (int i = 0; i < currentTimeApps.getInstanceDeployOnNode().length; i++) {
//                                    System.out.println(Arrays.toString(currentTimeApps.getInstanceDeployOnNode()[i]));
//                                }
                                gain = (total_delay - total_delay_migration) / e;
                                System.out.println("gain:" + gain);
                                migrationInfo.add(migrationInstanceNum);
                                migrationInfo.add(gain);
                                decreaseMigrationDestinationList.put(migrationDestinationNode,migrationInfo);
                            }
                            Map<Integer, List<Object>> sortedMap = sortMapByDelayGainDesc(decreaseMigrationDestinationList);

                            // 输出结果
                            for (Map.Entry<Integer, List<Object>> decreaseMigrationDestinationEntry : sortedMap.entrySet()) {
                                System.out.println("migrationDestinationNode: " + decreaseMigrationDestinationEntry.getKey() + ", migrationInstanceNum: " + decreaseMigrationDestinationEntry.getValue().get(0) + ", Delay Gain: " + decreaseMigrationDestinationEntry.getValue().get(1));
                            }
                            for (Map.Entry<Integer, List<Object>> decreaseMigrationDestinationEntry : sortedMap.entrySet()) {
                                System.out.println("进行正式迁移操作");
                                int migrationDestinationNode = decreaseMigrationDestinationEntry.getKey();//转入的节点id
                                int migrationInstanceNum = (int)decreaseMigrationDestinationEntry.getValue().get(0);//转入的实例数量
                                int migrationNum = instanceDeployOnNode[migrationNode][migrationService] > migrationInstanceNum ? migrationInstanceNum : instanceDeployOnNode[migrationNode][migrationService];
                                migrationCost.set(migrationService, (double) (migrationNum/instanceDeployOnNode[migrationNode][migrationService])*alltimeApp.get(time).getArrivalRate_matrix()[migrationNode][migrationService]);
                                instanceDeployOnNode[migrationDestinationNode][migrationService] += migrationNum;
                                instanceDeployOnNode[migrationNode][migrationService] -= migrationNum;
                                instanceToTighten -= migrationNum;
                                double pastMigrationCost = migrationCost.get(migrationService);

                                if(instanceToTighten <= 0){
                                    //遍历每条请求流
                                    for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                                        int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                                        for (PathProbability one_mspath : alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities()) {
                                            List<List<List<Object>>> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                                        }
                                    }
                                    alltimeApp.get(time).genBandwidthResourceAndArrivalmatrix(appParams);
                                    break;
                                }
                            }
                            if(instanceToTighten > 0){
                                //找到一个新的服务器节点
                                int migrationDestinationNode = FindNewNodeToActivate(instanceDeployOnNode,appParams,migrationNode);
                                //直接分配实例资源
                                migrationCost.set(migrationService, (double) (instanceToTighten/instanceDeployOnNode[migrationNode][migrationService])*alltimeApp.get(time).getArrivalRate_matrix()[migrationNode][migrationService]);
                                instanceDeployOnNode[migrationDestinationNode][migrationService] += instanceToTighten;
                                instanceDeployOnNode[migrationNode][migrationService] -= instanceToTighten;

                                instanceToTighten = 0;//被完全部署
                                //遍历每条请求流
                                for (int i = 0; i < alltimeApp.get(time).getAppPathInfos().size(); i++) {
                                    int[][] InstanceDeployOnNode = alltimeApp.get(time).getInstanceDeployOnNode();
                                    for (PathProbability one_mspath : alltimeApp.get(time).getAppPathInfos().get(i).getPathProbabilities()) {
                                        List<List<List<Object>>> Routing_tables_eachPath = one_mspath.genPathRouting_tables(InstanceDeployOnNode);
                                    }
                                }
                                alltimeApp.get(time).genBandwidthResourceAndArrivalmatrix(appParams);
                                break;
                            }
                        }
                    }
                }
            }
            alltimeApp.get(time).setMigrationCost(migrationCost);
            System.out.println("当前迁移成本MC(t):" + migrationCost);
            //打印部署结果
            System.out.println("当前迁移后部署情况X(t):");
            for (int i = 0; i < alltimeApp.get(time).getInstanceDeployOnNode().length; i++) {
                System.out.println(Arrays.toString(alltimeApp.get(time).getInstanceDeployOnNode()[i]));
            }
        }
        return alltimeApp;
    }

    /**
     * @Description : map，其value值List<Object>的内容包含了两项，list.get(0)存储的是int类型的节点id信息，list.get(1)存储的是double类型的时延增益信息，请你帮我根据每个list的时延增益信息将map进行降序排序
     * @Author : Dior
     *  * @param map
     * @return : java.util.Map<java.lang.Integer,java.util.List<java.lang.Object>>
     * @Date : 2024/7/24
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<Integer, List<Object>> sortMapByDelayGainDesc(Map<Integer, List<Object>> map) {
        // 将Map的条目存入List
        List<Map.Entry<Integer, List<Object>>> entryList = new ArrayList<>(map.entrySet());

        // 使用Comparator按时延增益进行降序排序
        entryList.sort((entry1, entry2) -> {
            Double delayGain1 = (Double) entry1.getValue().get(1);
            Double delayGain2 = (Double) entry2.getValue().get(1);
            return delayGain2.compareTo(delayGain1);
        });

        // 将排序后的条目存入LinkedHashMap以保持顺序
        Map<Integer, List<Object>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Object>> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static int CalNodeAccessibleResource (int[][] instanceDeployOnNode,int nodeResource,int nodeID,int serviceNum) {
        int nodeAccessibleResource = nodeResource;
        for(int service = 0;service < serviceNum;service ++){
            nodeAccessibleResource -= instanceDeployOnNode[nodeID][service];
        }
        return nodeAccessibleResource;
    }


    /**
     * @Description : 计算迁移的权重参数
     * @Author : Dior
     *  * @param utilizationActive
     * @param totalServiceInstances
     * @param migrationNode
     * @param PhysicalConnectionDelay
     * @return : java.util.Map<java.lang.Integer,java.lang.Double>
     * @Date : 2024/7/23
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<Integer, Double> calculateWeightedSumMigration(Map<Integer, Double> utilizationActive, Map<Integer, Integer> totalServiceInstances, int migrationNode,double[][] PhysicalConnectionDelay) {
        int nodeNum = utilizationActive.size();
        Map<Integer, Double> weightedSumNode = new HashMap<>();
        double coefficient1 = 0.5;
        double coefficient2 =0.5;
        for (int node = 0; node < nodeNum; node++) {
            Map.Entry<Integer, Double> utilizationEntry = getEntryByKeyDouble(utilizationActive, node);
            Map.Entry<Integer, Integer> serviceInstancesEntry = getEntryByKey(totalServiceInstances, node);
            if(utilizationEntry.getValue()!=0) {
                double weightedSum = coefficient1 * serviceInstancesEntry.getValue() - coefficient2 * PhysicalConnectionDelay[node][migrationNode];
                weightedSumNode.put(node, weightedSum);
            }
        }
        sortByValuesDescendingDouble(weightedSumNode);
        return weightedSumNode;
    }

    public static Map<Integer, Integer> mergeMaps(Map<Integer, Integer> map1, Map<Integer, Integer> map2) {
        // 创建一个新Map来存储合并后的结果
        Map<Integer, Integer> mergedMap = new HashMap<>(map1);

        // 遍历第二个Map，将值合并到第一个Map中
        for (Map.Entry<Integer, Integer> entry : map2.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            mergedMap.merge(key, value, Integer::sum);
        }

        return mergedMap;
    }



    /**
     * @Description : 将数据通信量集合
     * @Author : Dior
     *  * @param migrationList
     * @return : java.util.List<java.util.List<java.lang.Integer>>
     * @Date : 2024/7/23
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static List<List<Object>> MergeTopologyPairs(List<List<Object>> topologyPairs) {
        // 使用Map来合并具有相同起点和终点的拓扑对
        Map<String, List<Object>> mergedMap = new HashMap<>();

        for (List<Object> migrationPair : topologyPairs) {
            int startMicroservice = (int) migrationPair.get(0);
            int startNode = (int) migrationPair.get(1);
            int endMicroservice = (int) migrationPair.get(2);
            int endNode = (int) migrationPair.get(3);
            double arrivalRate = (double) migrationPair.get(4);

            String key = startMicroservice + "-" + startNode + "-" + endMicroservice + "-" + endNode;

            if (mergedMap.containsKey(key)) {
                List<Object> existingPair = mergedMap.get(key);
                double existingArrivalRate = (double) existingPair.get(4);
                existingPair.set(4, existingArrivalRate + arrivalRate);
            } else {
                mergedMap.put(key, new ArrayList<>(migrationPair));
            }
        }

        // 将合并后的结果转换回List<List<Object>>
        return new ArrayList<>(mergedMap.values());
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
     * @Description : 将特定类型map按照value值进行升序排序
     * @Author : Dior
     *  * @param bandwidthResource
     * @return : java.util.Map<utils.Main_Algorithm.NodePair,java.lang.Double>
     * @Date : 2024/7/22
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<NodePair, Double> sortBandwidthResource(double[][] bandwidthResource) {
        Map<NodePair, Double> bandwidthMap = new HashMap<>();
        int numNodes = bandwidthResource.length;
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                NodePair key = new NodePair(i, j);
                double bandwidth = bandwidthResource[i][j];
                bandwidthMap.put(key, bandwidth);
            }
        }

        // 按value值进行升序排序
        Map<NodePair, Double> sortedBandwidthMap = bandwidthMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return sortedBandwidthMap;
    }

    /**
     * @Description : 该类存储key值的节点对
     * @Author : Dior
     *  * @param null
     * @return :
     * @Date : 2024/7/22
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static class NodePair {
        private final int node1;
        private final int node2;

        public NodePair(int node1, int node2) {
            this.node1 = node1;
            this.node2 = node2;
        }


        public int getNode1() {
            return node1;
        }

        public int getNode2() {
            return node2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodePair nodePair = (NodePair) o;
            return (node1 == nodePair.node1 && node2 == nodePair.node2) ||
                    (node1 == nodePair.node2 && node2 == nodePair.node1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(Math.min(node1, node2), Math.max(node1, node2));
        }

        @Override
        public String toString() {
            return "NodePair{" +
                    "node1=" + node1 +
                    ", node2=" + node2 +
                    '}';
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
     * @Description : 初始化map 便于后续进行内容替换
     * @Author : Dior
     *  * @param appParams
     * @return : java.util.Map<java.lang.Integer,java.util.Map<java.lang.Integer,java.lang.Integer>>
     * @Date : 2024/7/17
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<Integer,Integer> initMapInt(App_Params appParams) {
        Map<Integer,Integer> ServiceInstance = new HashMap<>();
        for (int node = 0; node < appParams.getNum_Server(); node++){
            ServiceInstance.put(node,0);
        }
        return ServiceInstance;
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
    public static Map<Integer,Double> initMapDouble(App_Params appParams) {
        Map<Integer,Double> ServiceInstance = new HashMap<>();
        for (int node = 0; node < appParams.getNum_Server(); node++){
            ServiceInstance.put(node, 0.0);
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
     * @Description : 将当前map按照value值升序排序
     * @Author : Dior
     *  * @param map
     * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
     * @Date : 2024/7/17
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static Map<Integer, Double> sortByValuesDescendingDouble(Map<Integer, Double> map) {
        // 将Map的Entry放入List
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());

        // 按值进行升序排序
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

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

}
