package utils;
import java.util.List;
import java.util.Random;

import static utils.Constant.NO_OF_MICROSERVICE;
import static utils.Constant.PROCESSING_RATE;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 15:01
 **/

public class App_Params {
    // 属性定义

    private int Num_Server; // 服务器节点的数量
    private int Num_Microservice; // 微服务的种类
    private List<ServiceTypeInfo> serviceTypeInfos; //所有微服务的信息
    private int Num_Application; // 基于微服务的应用的种类
    private int Num_Time_Slot; // 时隙的长度
    private int Num_CPU_Core; // 每个服务器的核心
    private int MAX1; // 最大值

    // 基于微服务的应用的各个参数范围
    private int[] App_Num; // 该时段下app的数量
    private int[] TTL_Max_Tolerance_Latency_Range; // 应用的生命周期/最大容忍时延范围
    private double[] Unit_Rate_Bandwidth_Range; // 单位到达率下对应的带宽占用范围
    private int[] Average_Arrival_Rate_Range; // 平均请求到达率的范围
    private int[] Num_Node_Range; // 微服务应用的节点个数范围
    private int[] Num_Edge_Range; // 微服务应用的边的范围满足的条件是 N~N(N-1)/2
    private int[] DAG_Category_Range; // DAG的种类：并行调用/概率转发
    private int[] Num_Apps_Timeslot_Range; // 每个时隙内待编排的微服务应用的数量
    private int[] Microservice_Type_CPU; // 每种微服务的一个实例需要消耗的CPU数量
    private int[] Microservice_Type_Memory; // 每种微服务的一个实例需要消耗的内存大小

    private double Lowest_Communication_Latency; // 服务期间通信时延的最低值1
    private double Highest_Communication_Latency; // 服务器间通信时延的最高值1
    private int Lowest_Bandwidth_Capacity; // 服务器间可用带宽最低值1
    private int Highest_Bandwidth_Capacity; // 服务器间可用带宽最高值1
    private int Lowest_Microservice_Bandwidth_Requirement; // 微服务间需求带宽最低值
    private int Highest_Microservice_Bandwidth_Requirement; // 微服务间需求带宽最高值

    private int Lowest_Microservice_Type_Unit_Process_Ability; // 微服务一个实例的最低处理能力1
    private int Highest_Microservice_Type_Unit_Process_Ability; // 微服务一个实例的最高处理能力1
    private double[][] PhysicalConnectionDelay;
    private double[][] PhysicalConnectionBandwidth;
    private double[][] MicroServiceConnectionDelay;


    public App_Params() {
    }

    public App_Params(int Num_Server, int Num_Microservice, List<ServiceTypeInfo> serviceTypeInfos, int Num_Application, int Num_Time_Slot, int Num_CPU_Core, int MAX1, int[] App_Num, int[] TTL_Max_Tolerance_Latency_Range, double[] Unit_Rate_Bandwidth_Range, int[] Average_Arrival_Rate_Range, int[] Num_Node_Range, int[] Num_Edge_Range, int[] DAG_Category_Range, int[] Num_Apps_Timeslot_Range, int[] Microservice_Type_CPU, int[] Microservice_Type_Memory, double Lowest_Communication_Latency, double Highest_Communication_Latency, int Lowest_Bandwidth_Capacity, int Highest_Bandwidth_Capacity, int Lowest_Microservice_Bandwidth_Requirement, int Highest_Microservice_Bandwidth_Requirement, int Lowest_Microservice_Type_Unit_Process_Ability, int Highest_Microservice_Type_Unit_Process_Ability) {
        this.Num_Server = Num_Server;
        this.Num_Microservice = Num_Microservice;
        this.serviceTypeInfos = serviceTypeInfos;
        this.Num_Application = Num_Application;
        this.Num_Time_Slot = Num_Time_Slot;
        this.Num_CPU_Core = Num_CPU_Core;
        this.MAX1 = MAX1;
        this.App_Num = App_Num;
        this.TTL_Max_Tolerance_Latency_Range = TTL_Max_Tolerance_Latency_Range;
        this.Unit_Rate_Bandwidth_Range = Unit_Rate_Bandwidth_Range;
        this.Average_Arrival_Rate_Range = Average_Arrival_Rate_Range;
        this.Num_Node_Range = Num_Node_Range;
        this.Num_Edge_Range = Num_Edge_Range;
        this.DAG_Category_Range = DAG_Category_Range;
        this.Num_Apps_Timeslot_Range = Num_Apps_Timeslot_Range;
        this.Microservice_Type_CPU = Microservice_Type_CPU;
        this.Microservice_Type_Memory = Microservice_Type_Memory;
        this.Lowest_Communication_Latency = Lowest_Communication_Latency;
        this.Highest_Communication_Latency = Highest_Communication_Latency;
        this.Lowest_Bandwidth_Capacity = Lowest_Bandwidth_Capacity;
        this.Highest_Bandwidth_Capacity = Highest_Bandwidth_Capacity;
        this.Lowest_Microservice_Bandwidth_Requirement = Lowest_Microservice_Bandwidth_Requirement;
        this.Highest_Microservice_Bandwidth_Requirement = Highest_Microservice_Bandwidth_Requirement;
        this.Lowest_Microservice_Type_Unit_Process_Ability = Lowest_Microservice_Type_Unit_Process_Ability;
        this.Highest_Microservice_Type_Unit_Process_Ability = Highest_Microservice_Type_Unit_Process_Ability;
    }


    public void CreateServiceList(){
        for(int i = 0;i < Num_Microservice;i++){
            Random r = new Random(100);//处理率的随机种子固定
            int servicestate = r.nextInt(2);//不同时隙的微服务
            System.out.println("============MS_PROCESSING_RATE=============");
            int processingrate = r.nextInt(5) + 1;
            ServiceTypeInfo serviceTypeInfo = new ServiceTypeInfo(i,servicestate,processingrate);
            this.serviceTypeInfos.add(serviceTypeInfo);
        }
    }

    public void InitNodeList(App_Params appParams){
        for(int i = 0;i < appParams.getNum_Server();i++){
            PhysicalNodeInfo physicalNodeInfo = new PhysicalNodeInfo();
            physicalNodeInfo.setNodeID(i);
        }
    }




    /**
     * 获取
     * @return Num_Server
     */
    public int getNum_Server() {
        return Num_Server;
    }

    /**
     * 设置
     * @param Num_Server
     */
    public void setNum_Server(int Num_Server) {
        this.Num_Server = Num_Server;
    }

    /**
     * 获取
     * @return Num_Microservice
     */
    public int getNum_Microservice() {
        return Num_Microservice;
    }

    /**
     * 设置
     * @param Num_Microservice
     */
    public void setNum_Microservice(int Num_Microservice) {
        this.Num_Microservice = Num_Microservice;
    }

    /**
     * 获取
     * @return serviceTypeInfos
     */
    public List<ServiceTypeInfo> getServiceTypeInfos() {
        return serviceTypeInfos;
    }

    /**
     * 设置
     * @param serviceTypeInfos
     */
    public void setServiceTypeInfos(List<ServiceTypeInfo> serviceTypeInfos) {
        this.serviceTypeInfos = serviceTypeInfos;
    }

    /**
     * 获取
     * @return Num_Application
     */
    public int getNum_Application() {
        return Num_Application;
    }

    /**
     * 设置
     * @param Num_Application
     */
    public void setNum_Application(int Num_Application) {
        this.Num_Application = Num_Application;
    }

    /**
     * 获取
     * @return Num_Time_Slot
     */
    public int getNum_Time_Slot() {
        return Num_Time_Slot;
    }

    /**
     * 设置
     * @param Num_Time_Slot
     */
    public void setNum_Time_Slot(int Num_Time_Slot) {
        this.Num_Time_Slot = Num_Time_Slot;
    }

    /**
     * 获取
     * @return Num_CPU_Core
     */
    public int getNum_CPU_Core() {
        return Num_CPU_Core;
    }

    /**
     * 设置
     * @param Num_CPU_Core
     */
    public void setNum_CPU_Core(int Num_CPU_Core) {
        this.Num_CPU_Core = Num_CPU_Core;
    }

    /**
     * 获取
     * @return MAX1
     */
    public int getMAX1() {
        return MAX1;
    }

    /**
     * 设置
     * @param MAX1
     */
    public void setMAX1(int MAX1) {
        this.MAX1 = MAX1;
    }

    /**
     * 获取
     * @return TTL_Max_Tolerance_Latency_Range
     */
    public int[] getTTL_Max_Tolerance_Latency_Range() {
        return TTL_Max_Tolerance_Latency_Range;
    }

    /**
     * 设置
     * @param TTL_Max_Tolerance_Latency_Range
     */
    public void setTTL_Max_Tolerance_Latency_Range(int[] TTL_Max_Tolerance_Latency_Range) {
        this.TTL_Max_Tolerance_Latency_Range = TTL_Max_Tolerance_Latency_Range;
    }

    /**
     * 获取
     * @return Unit_Rate_Bandwidth_Range
     */
    public double[] getUnit_Rate_Bandwidth_Range() {
        return Unit_Rate_Bandwidth_Range;
    }

    /**
     * 设置
     * @param Unit_Rate_Bandwidth_Range
     */
    public void setUnit_Rate_Bandwidth_Range(double[] Unit_Rate_Bandwidth_Range) {
        this.Unit_Rate_Bandwidth_Range = Unit_Rate_Bandwidth_Range;
    }

    /**
     * 获取
     * @return Average_Arrival_Rate_Range
     */
    public int[] getAverage_Arrival_Rate_Range() {
        return Average_Arrival_Rate_Range;
    }

    /**
     * 设置
     * @param Average_Arrival_Rate_Range
     */
    public void setAverage_Arrival_Rate_Range(int[] Average_Arrival_Rate_Range) {
        this.Average_Arrival_Rate_Range = Average_Arrival_Rate_Range;
    }

    /**
     * 获取
     * @return Num_Node_Range
     */
    public int[] getNum_Node_Range() {
        return Num_Node_Range;
    }

    /**
     * 设置
     * @param Num_Node_Range
     */
    public void setNum_Node_Range(int[] Num_Node_Range) {
        this.Num_Node_Range = Num_Node_Range;
    }

    /**
     * 获取
     * @return Num_Edge_Range
     */
    public int[] getNum_Edge_Range() {
        return Num_Edge_Range;
    }

    /**
     * 设置
     * @param Num_Edge_Range
     */
    public void setNum_Edge_Range(int[] Num_Edge_Range) {
        this.Num_Edge_Range = Num_Edge_Range;
    }

    /**
     * 获取
     * @return DAG_Category_Range
     */
    public int[] getDAG_Category_Range() {
        return DAG_Category_Range;
    }

    /**
     * 设置
     * @param DAG_Category_Range
     */
    public void setDAG_Category_Range(int[] DAG_Category_Range) {
        this.DAG_Category_Range = DAG_Category_Range;
    }

    /**
     * 获取
     * @return Num_Apps_Timeslot_Range
     */
    public int[] getNum_Apps_Timeslot_Range() {
        return Num_Apps_Timeslot_Range;
    }

    /**
     * 设置
     * @param Num_Apps_Timeslot_Range
     */
    public void setNum_Apps_Timeslot_Range(int[] Num_Apps_Timeslot_Range) {
        this.Num_Apps_Timeslot_Range = Num_Apps_Timeslot_Range;
    }

    /**
     * 获取
     * @return Microservice_Type_CPU
     */
    public int[] getMicroservice_Type_CPU() {
        return Microservice_Type_CPU;
    }

    /**
     * 设置
     * @param Microservice_Type_CPU
     */
    public void setMicroservice_Type_CPU(int[] Microservice_Type_CPU) {
        this.Microservice_Type_CPU = Microservice_Type_CPU;
    }

    /**
     * 获取
     * @return Microservice_Type_Memory
     */
    public int[] getMicroservice_Type_Memory() {
        return Microservice_Type_Memory;
    }

    /**
     * 设置
     * @param Microservice_Type_Memory
     */
    public void setMicroservice_Type_Memory(int[] Microservice_Type_Memory) {
        this.Microservice_Type_Memory = Microservice_Type_Memory;
    }

    /**
     * 获取
     * @return Lowest_Communication_Latency
     */
    public double getLowest_Communication_Latency() {
        return Lowest_Communication_Latency;
    }

    /**
     * 设置
     * @param Lowest_Communication_Latency
     */
    public void setLowest_Communication_Latency(double Lowest_Communication_Latency) {
        this.Lowest_Communication_Latency = Lowest_Communication_Latency;
    }

    /**
     * 获取
     * @return Highest_Communication_Latency
     */
    public double getHighest_Communication_Latency() {
        return Highest_Communication_Latency;
    }

    /**
     * 设置
     * @param Highest_Communication_Latency
     */
    public void setHighest_Communication_Latency(double Highest_Communication_Latency) {
        this.Highest_Communication_Latency = Highest_Communication_Latency;
    }

    /**
     * 获取
     * @return Lowest_Bandwidth_Capacity
     */
    public int getLowest_Bandwidth_Capacity() {
        return Lowest_Bandwidth_Capacity;
    }

    /**
     * 设置
     * @param Lowest_Bandwidth_Capacity
     */
    public void setLowest_Bandwidth_Capacity(int Lowest_Bandwidth_Capacity) {
        this.Lowest_Bandwidth_Capacity = Lowest_Bandwidth_Capacity;
    }

    /**
     * 获取
     * @return Highest_Bandwidth_Capacity
     */
    public int getHighest_Bandwidth_Capacity() {
        return Highest_Bandwidth_Capacity;
    }

    /**
     * 设置
     * @param Highest_Bandwidth_Capacity
     */
    public void setHighest_Bandwidth_Capacity(int Highest_Bandwidth_Capacity) {
        this.Highest_Bandwidth_Capacity = Highest_Bandwidth_Capacity;
    }

    /**
     * 获取
     * @return Lowest_Microservice_Bandwidth_Requirement
     */
    public int getLowest_Microservice_Bandwidth_Requirement() {
        return Lowest_Microservice_Bandwidth_Requirement;
    }

    /**
     * 设置
     * @param Lowest_Microservice_Bandwidth_Requirement
     */
    public void setLowest_Microservice_Bandwidth_Requirement(int Lowest_Microservice_Bandwidth_Requirement) {
        this.Lowest_Microservice_Bandwidth_Requirement = Lowest_Microservice_Bandwidth_Requirement;
    }

    /**
     * 获取
     * @return Highest_Microservice_Bandwidth_Requirement
     */
    public int getHighest_Microservice_Bandwidth_Requirement() {
        return Highest_Microservice_Bandwidth_Requirement;
    }

    /**
     * 设置
     * @param Highest_Microservice_Bandwidth_Requirement
     */
    public void setHighest_Microservice_Bandwidth_Requirement(int Highest_Microservice_Bandwidth_Requirement) {
        this.Highest_Microservice_Bandwidth_Requirement = Highest_Microservice_Bandwidth_Requirement;
    }

    /**
     * 获取
     * @return Lowest_Microservice_Type_Unit_Process_Ability
     */
    public int getLowest_Microservice_Type_Unit_Process_Ability() {
        return Lowest_Microservice_Type_Unit_Process_Ability;
    }

    /**
     * 设置
     * @param Lowest_Microservice_Type_Unit_Process_Ability
     */
    public void setLowest_Microservice_Type_Unit_Process_Ability(int Lowest_Microservice_Type_Unit_Process_Ability) {
        this.Lowest_Microservice_Type_Unit_Process_Ability = Lowest_Microservice_Type_Unit_Process_Ability;
    }

    /**
     * 获取
     * @return Highest_Microservice_Type_Unit_Process_Ability
     */
    public int getHighest_Microservice_Type_Unit_Process_Ability() {
        return Highest_Microservice_Type_Unit_Process_Ability;
    }

    /**
     * 设置
     * @param Highest_Microservice_Type_Unit_Process_Ability
     */
    public void setHighest_Microservice_Type_Unit_Process_Ability(int Highest_Microservice_Type_Unit_Process_Ability) {
        this.Highest_Microservice_Type_Unit_Process_Ability = Highest_Microservice_Type_Unit_Process_Ability;
    }

    /**
     * 获取
     * @return App_Num
     */
    public int[] getApp_Num() {
        return App_Num;
    }

    /**
     * 设置
     * @param App_Num
     */
    public void setApp_Num(int[] App_Num) {
        this.App_Num = App_Num;
    }

    public String toString() {
        return "App_Params{Num_Server = " + Num_Server + ", Num_Microservice = " + Num_Microservice + ", serviceTypeInfos = " + serviceTypeInfos + ", Num_Application = " + Num_Application + ", Num_Time_Slot = " + Num_Time_Slot + ", Num_CPU_Core = " + Num_CPU_Core + ", MAX1 = " + MAX1 + ", App_Num = " + App_Num + ", TTL_Max_Tolerance_Latency_Range = " + TTL_Max_Tolerance_Latency_Range + ", Unit_Rate_Bandwidth_Range = " + Unit_Rate_Bandwidth_Range + ", Average_Arrival_Rate_Range = " + Average_Arrival_Rate_Range + ", Num_Node_Range = " + Num_Node_Range + ", Num_Edge_Range = " + Num_Edge_Range + ", DAG_Category_Range = " + DAG_Category_Range + ", Num_Apps_Timeslot_Range = " + Num_Apps_Timeslot_Range + ", Microservice_Type_CPU = " + Microservice_Type_CPU + ", Microservice_Type_Memory = " + Microservice_Type_Memory + ", Lowest_Communication_Latency = " + Lowest_Communication_Latency + ", Highest_Communication_Latency = " + Highest_Communication_Latency + ", Lowest_Bandwidth_Capacity = " + Lowest_Bandwidth_Capacity + ", Highest_Bandwidth_Capacity = " + Highest_Bandwidth_Capacity + ", Lowest_Microservice_Bandwidth_Requirement = " + Lowest_Microservice_Bandwidth_Requirement + ", Highest_Microservice_Bandwidth_Requirement = " + Highest_Microservice_Bandwidth_Requirement + ", Lowest_Microservice_Type_Unit_Process_Ability = " + Lowest_Microservice_Type_Unit_Process_Ability + ", Highest_Microservice_Type_Unit_Process_Ability = " + Highest_Microservice_Type_Unit_Process_Ability + "}";
    }
}
