package utils;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.util.*;

import static utils.WaitingTime.calculateMicroserviceNodeAverageServiceTime;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-12 20:34
 **/
public class InstanceCalculator {
    public static ArrayList<CurrentTimeApps> CalculateInstance(App_Params appParams,ArrayList<CurrentTimeApps> alltimeApp) {
        //后面开始准备计算实例需求
        for(int time = 0; time < alltimeApp.size(); time++){
            ArrayList<AppPathInfo> currentAppList = alltimeApp.get(time).getAppPathInfos();
            ArrayList<Integer> serviceInstanceNum = new ArrayList<>(appParams.getNum_Microservice());//存储每个节点的微服务实例部署情况
            for (int i = 0; i < appParams.getNum_Microservice(); i++) {
                serviceInstanceNum.add(0); // 或者任何其他默认值
            }
            System.out.println("开始处理时隙"+time+"的所有APP");
            System.out.println("对该时隙APP按照请求到达率大小进行排序");
            // 使用Collections.sort()和自定义Comparator进行排序
            Collections.sort(currentAppList, new Comparator<AppPathInfo>() {
                @Override
                public int compare(AppPathInfo o1, AppPathInfo o2) {
                    // 从高到低排序
                    return Double.compare(o2.getArrivalRate(), o1.getArrivalRate());
                }
            });
            System.out.println("排序结果如下");
            for (AppPathInfo app : currentAppList) {
                System.out.println(app.getArrivalRate());
            }

            for (AppPathInfo app : currentAppList) {//该循环方式修改app的属性会对currentAppList中的app对象属性进行修改
                System.out.println("当前APP的类型是" + app.getAppType());
                List<PathProbability> pathProbabilities = app.getPathProbabilities();
                System.out.println("计算该app下每个路径的到达率");
                for (PathProbability pathProbability : pathProbabilities) {
                    pathProbability.setArrivalRate(app.getArrivalRate()*pathProbability.getProbability());
                }
                System.out.println("对该app的每条路径的到达率进行排序");
                Collections.sort(pathProbabilities, new Comparator<PathProbability>() {
                    @Override
                    public int compare(PathProbability o1, PathProbability o2) {
                        // 从高到低排序
                        return Double.compare(o2.getArrivalRate(), o1.getArrivalRate());
                    }
                });
                System.out.println("排序结果如下");
                for (PathProbability pathProbability : pathProbabilities) {
                    System.out.println(pathProbability.getArrivalRate());
                }
                int roundRobinParam = appParams.getRoundRobinParam();//获取轮询队列数量
                for (PathProbability pathProbability : pathProbabilities) {
                    double idealLatency = 0;
                    double arrivalRatePath = pathProbability.getArrivalRate();
                    //计算收紧的容忍时延
                    double appTighterToleranceDelay = app.getAppMaxToleranceLatency()-(pathProbability.getNodeInfos().size()-1)*
                            (appParams.getAvgPhysicalConnectionDelay()+arrivalRatePath*appParams.getAvgArrivalRateDataSize()/appParams.getAvgPhysicalConnectionBandwidth());
                    for(int currentNodeInfo = 0; currentNodeInfo < pathProbability.getNodeInfos().size(); currentNodeInfo++){
                        //计算一个基础的实例需求
                        int serviceType = pathProbability.getNodeInfos().get(currentNodeInfo).getServiceType();
                        int currentServiceProcessingRate = appParams.getServiceTypeInfos().get(serviceType).getServiceProcessingRate();
                        double currentArrivalRateOnNode = arrivalRatePath;
                        NodeInfo currentNode = pathProbability.getNodeInfos().get(currentNodeInfo);
                        currentNode.setArrivalRate_On_Node(currentArrivalRateOnNode);//该服务链当前节点的请求到达率即该请求链的到达率 他可以被拆分到不同的服务器节点
                        int temporaryInstance = (int)(currentArrivalRateOnNode/currentServiceProcessingRate) + 1;//粗略计算暂时的实例数量
                        ArrayList<Integer> deployedNode = new ArrayList<>();
                        for(int node = 0 ; node < appParams.getNum_Server() ; node++ ){
                            deployedNode.add(0);
                        }
                        currentNode.setDeployedNode(deployedNode);//最初实例只是计算 并未部署在某一结点上因此，全为0
                        if(app.getAppType()==0){
                            currentNode.setInstance_To_Deploy(currentNode.getInstance_To_Deploy() + temporaryInstance);
                        }else if(app.getAppType()==1){
                            if (temporaryInstance > currentNode.getInstance_To_Deploy()){
                                currentNode.setInstance_To_Deploy(temporaryInstance);
                            }
                        }
                        if(appParams.getServiceTypeInfos().get(serviceType).getServiceState() == 1){
                            //这边需要减去有状态的微服务与数据库交互的时延
                            appTighterToleranceDelay -= appParams.getDataBaseCommunicationDelay();
                        }
                        //这边同步利用排队网络计算时延计算调用链在理想网络状态下的首次响应时延，只包括平均处理时延，平均排队时延
                        idealLatency +=calculateMicroserviceNodeAverageServiceTime(currentArrivalRateOnNode,temporaryInstance,currentServiceProcessingRate);//这边计算当前节点的微服务的时延情况
                    }
                    pathProbability.setTighterToleranceLatency(appTighterToleranceDelay);
                    System.out.println("当前路径"+pathProbability.getNodeInfos());
                    while(idealLatency > appTighterToleranceDelay){
                        Map<Integer,Double> delayGain = new HashMap<>();//存储所有的增益并且绑定相应微服务实例，便于后续进行排序
                        for(int currentNodeInfoGain = 0; currentNodeInfoGain < pathProbability.getNodeInfos().size(); currentNodeInfoGain++){
                            //第一个循环用来计算增加一个微服务实例带来的时延增益
                            int currentNodeInfoGainService = pathProbability.getNodeInfos().get(currentNodeInfoGain).getServiceType();
                            int currentNodeInfoGainServiceInstance = pathProbability.getNodeInfos().get(currentNodeInfoGain).getInstance_To_Deploy();//保存最初的微服务实例数量 便于还原
                            double timeDelayGain = 0;//计算增益时延
                            Map<Integer ,ArrayList<Integer>> splitInstances = new HashMap<>();//存储第二个循环中保存的划分结果
                            pathProbability.getNodeInfos().get(currentNodeInfoGain).setInstance_To_Deploy(currentNodeInfoGainServiceInstance+1);//首先增加当前循环到的微服务的实例数量1个
                            for(int currentNodeInfoDelay = 0; currentNodeInfoDelay < pathProbability.getNodeInfos().size(); currentNodeInfoDelay++) {
                                //第二个循环是为了计算所有可能的路径来计算时延
                                int queueSize = 0;
                                int instanceNum = pathProbability.getNodeInfos().get(currentNodeInfoDelay).getInstance_To_Deploy();
                                if (instanceNum <= roundRobinParam) {
                                    queueSize = instanceNum;
                                } else {
                                    queueSize = roundRobinParam;
                                }
                                System.out.println("微服务"+pathProbability.getNodeInfos().get(currentNodeInfoDelay).getServiceType()+"的实例数量"+instanceNum+"的队列数量"+queueSize);
                                ArrayList<Integer> currentServiceSplitInstance = new ArrayList<>(Collections.nCopies(queueSize, 0));
                                for (int instance = 0; instance < instanceNum; instance++) {
                                    int queueIndex = instance % roundRobinParam;
                                    if (instance + 1 <= roundRobinParam) {
                                        currentServiceSplitInstance.set(queueIndex, 1);
                                    }else {
                                        currentServiceSplitInstance.set(queueIndex, (int) (instance / roundRobinParam) + 1);
                                    }
                                }
                                System.out.println(currentServiceSplitInstance);
                                splitInstances.put(pathProbability.getNodeInfos().get(currentNodeInfoDelay).getServiceType(), currentServiceSplitInstance);
                            }
                            //轮询放置完毕进行时延计算
                            List<TempPathInfo> pathInfos = calculatePathInfos(splitInstances,appParams,arrivalRatePath);
                            // 输出所有路径及其概率和时延
                            for (TempPathInfo pi : pathInfos) {
                                System.out.println("Path: " + pi.path + ", Probability: " + pi.probability + ", Latency: " + pi.latency);
                            }
                            double currentTighterDelay = 0;
                            // 按照概率计算时延
                            for (TempPathInfo pi : pathInfos) {
                                currentTighterDelay += pi.probability*pi.latency;
                            }
                            timeDelayGain = idealLatency - currentTighterDelay;//计算增益
                            pathProbability.getNodeInfos().get(currentNodeInfoGain).setInstance_To_Deploy(currentNodeInfoGainServiceInstance);
                            delayGain.put(currentNodeInfoGainService,timeDelayGain);
                        }
                        // 调用排序方法
                        Map<Integer, Double> sortedMap = sortByValueDescending(delayGain);
                        //获取到增益最大的map的第一个key，将其对应的实例+1
                        // 获取排序后的第一个Key
                        Map.Entry<Integer, Double> nodeServiceToIncrease = sortedMap.entrySet().iterator().next();
                        double updateDelay = nodeServiceToIncrease.getValue();
                        int targetServiceIndex = 0;
                        for(int targetNodeInfoIndex = 0; targetNodeInfoIndex < pathProbability.getNodeInfos().size(); targetNodeInfoIndex++){
                            if(pathProbability.getNodeInfos().get(targetNodeInfoIndex).getServiceType() == nodeServiceToIncrease.getKey()){
                                targetServiceIndex = targetNodeInfoIndex;
                                break;
                            }
                        }
                        int currentNodeInfoGainServiceInstance = pathProbability.getNodeInfos().get(targetServiceIndex).getInstance_To_Deploy();//重新获取微服务实例数量
                        System.out.println(currentNodeInfoGainServiceInstance);

                        if(app.getAppType()==0){
                            pathProbability.getNodeInfos().get(targetServiceIndex).setInstance_To_Deploy(currentNodeInfoGainServiceInstance+1);//更新需要部署的实例数量
                        }else if(app.getAppType()==1){
                            if (currentNodeInfoGainServiceInstance+1 > pathProbability.getNodeInfos().get(targetServiceIndex).getInstance_To_Deploy())
                                pathProbability.getNodeInfos().get(targetServiceIndex).setInstance_To_Deploy(currentNodeInfoGainServiceInstance+1);//更新需要部署的实例数量
                        }
                        idealLatency = 0;//更新计算新的ideallatency
                        for(int currentNodeInfo = 0; currentNodeInfo < pathProbability.getNodeInfos().size(); currentNodeInfo++){
                            //计算一个基础的实例需求
                            int serviceType = pathProbability.getNodeInfos().get(currentNodeInfo).getServiceType();
                            int currentServiceProcessingRate = appParams.getServiceTypeInfos().get(serviceType).getServiceProcessingRate();
                            double currentArrivalRateOnNode = pathProbability.getArrivalRate();
                            pathProbability.getNodeInfos().get(currentNodeInfo).setArrivalRate_On_Node(currentArrivalRateOnNode);//该服务链当前节点的请求到达率即该请求链的到达率 他可以被拆分到不同的服务器节点
                            //这边同步利用排队网络计算时延计算调用链在理想网络状态下的首次响应时延，只包括平均处理时延，平均排队时延
                            idealLatency +=calculateMicroserviceNodeAverageServiceTime(currentArrivalRateOnNode,pathProbability.getNodeInfos().get(currentNodeInfo).getInstance_To_Deploy(),currentServiceProcessingRate);//这边计算当前节点的微服务的时延情况
                        }
                    }
                }
            }
            alltimeApp.get(time).setAppPathInfos(currentAppList);//更新一个时隙后的app情况
            //更新一个时隙后的实例部署情况
            for (int i = 0; i < currentAppList.size(); i++) {
                Map<Integer, NodeInfo> nodeInfos = currentAppList.get(i).getNodeInfos();
                for (NodeInfo value : nodeInfos.values()) {
                    serviceInstanceNum.set(value.getServiceType(), serviceInstanceNum.get(value.getServiceType()) + value.getInstance_To_Deploy());
                }
            }
            alltimeApp.get(time).setServiceInstanceNum(serviceInstanceNum);
            System.out.println("serviceInstancNum:" + serviceInstanceNum);
        }
        return alltimeApp;
    }
    public static Map<Integer, Double> sortByValueDescending(Map<Integer, Double> map) {
        // 将Map的Entry放入List
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(map.entrySet());
        // 按值进行排序
        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));
        // 创建一个有序的LinkedHashMap
        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    public static List<TempPathInfo> calculatePathInfos(Map<Integer, ArrayList<Integer>> splitInstances, App_Params appParams, double arrivalRatePath) {
        List<TempPathInfo> results = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>();
        List<Integer> serviceIds = new ArrayList<>(splitInstances.keySet());
        calculatePathInfosRecursive(splitInstances,appParams,arrivalRatePath, serviceIds, results, currentPath, 1.0, 0, 0.0);
        return results;
    }
    private static void calculatePathInfosRecursive(Map<Integer, ArrayList<Integer>> splitInstances, App_Params appParams, double arrivalRatePath, List<Integer> serviceIds, List<TempPathInfo> results,
                                                    List<Integer> currentPath, double currentProbability, int serviceIndex, double currentLatency) {
        if (serviceIndex >= serviceIds.size()) {
            results.add(new TempPathInfo(new ArrayList<>(currentPath), currentProbability, currentLatency));
            return;
        }

        int serviceId = serviceIds.get(serviceIndex);
        ArrayList<Integer> instanceCounts = splitInstances.get(serviceId);
        int totalInstances = instanceCounts.stream().mapToInt(Integer::intValue).sum();
        System.out.println("当前计算微服务"+serviceId+"的时延");
        int currentServiceProcessingRate = appParams.getServiceTypeInfos().get(serviceId).getServiceProcessingRate();
        for (int i = 0; i < instanceCounts.size(); i++) {
            int queueInstance = instanceCounts.get(i);
            currentPath.add(i);
            double newqueueProbability = currentProbability * ((double) queueInstance / totalInstances);
            double newLatency = currentLatency + calculateMicroserviceNodeAverageServiceTime(newqueueProbability*arrivalRatePath, queueInstance, currentServiceProcessingRate); // 时延计算
            calculatePathInfosRecursive(splitInstances, appParams, arrivalRatePath, serviceIds, results, currentPath, newqueueProbability, serviceIndex + 1, newLatency);
            currentPath.remove(currentPath.size() - 1);
        }
    }
}
