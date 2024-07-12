package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
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
        appParams.setDataBaseCommunicationDelay(0.1);//不知道要不要写到appParams中作为常量还是后续要详细计算 目前当常量考虑
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
        double avgPhysicalConnectionDelay = appParams.getAvgPhysicalConnectionDelay();
        int avgPhysicalConnectionBandwidth = appParams.getAvgPhysicalConnectionBandwidth();
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
        ArrayList<CurrentTimeApps> alltimeApp = GenerateApp.CreatealltimeApp(timeslot,appParams,r);

        //后面开始准备计算实例需求
        alltimeApp = InstanceCalculator.CreatealltimeApp(appParams,alltimeApp);

        //准备部署实例
        for(int time = 0; time < alltimeApp.size(); time++){
            ArrayList<Integer> NowServiceInstanceNum = new ArrayList<>(appParams.getNum_Microservice());//获取当前时隙的app情况
            ArrayList<Integer> PastServiceInstanceNum = new ArrayList<>(appParams.getNum_Microservice());//获取前一个时隙后的app情况
            if(time == 0) {
                NowServiceInstanceNum = alltimeApp.get(time).getServiceInstanceNum();//获取当前时隙的app情况
                //在第一个时隙，其不存在前一个时隙的部署,即所有均为0
                for (int i = 0; i < appParams.getNum_Microservice(); i++) {
                    PastServiceInstanceNum.add(0); // 或者任何其他默认值
                }
            }else {
                NowServiceInstanceNum = alltimeApp.get(time).getServiceInstanceNum();//获取当前时隙的app情况
                PastServiceInstanceNum = alltimeApp.get(time-1).getServiceInstanceNum();//获取前一个时隙后的app情况
            }
        }
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
