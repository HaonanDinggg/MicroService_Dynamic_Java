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
    private int[] Arrival_Time_Range; // 请求流到达时间的范围
    private int[] TTL_Max_Tolerance_Latency_Range; // 应用的生命周期/最大容忍时延范围
    private double[] Unit_Rate_Bandwidth_Range; // 单位到达率下对应的带宽占用范围
    private int[] Average_Arrival_Rate_Range; // 平均请求到达率的范围
    private int[] Num_Node_Range; // 微服务应用的节点个数范围
    private int[] Num_Edge_Range; // 微服务应用的边的范围满足的条件是 N~N(N-1)/2
    private int[] DAG_Category_Range; // DAG的种类：并行调用/概率转发
    private int[] Num_Apps_Timeslot_Range; // 每个时隙内待编排的微服务应用的数量

    private int[] Microservice_Type_CPU; // 每种微服务的一个实例需要消耗的CPU数量
    private int[] Microservice_Type_Memory; // 每种微服务的一个实例需要消耗的内存大小

    private double Lowest_Communication_Latency; // 服务期间通信时延的最低值
    private double Highest_Communication_Latency; // 服务器间通信时延的最高值
    private int Lowest_Bandwidth_Capacity; // 服务器间可用带宽最低值
    private int Highest_Bandwidth_Capacity; // 服务器间可用带宽最高值
    private int Lowest_Microservice_Bandwidth_Requirement; // 微服务间需求带宽最低值
    private int Highest_Microservice_Bandwidth_Requirement; // 微服务间需求带宽最高值

    private int Lowest_Microservice_Type_Unit_Process_Ability; // 微服务一个实例的最低处理能力
    private int Highest_Microservice_Type_Unit_Process_Ability; // 微服务一个实例的最高处理能力

    public App_Params() {
    }

    public App_Params(int Num_Server, int Num_Microservice, List<ServiceTypeInfo> serviceTypeInfos, int Num_Application, int Num_Time_Slot, int Num_CPU_Core, int MAX1, int[] Arrival_Time_Range, int[] TTL_Max_Tolerance_Latency_Range, double[] Unit_Rate_Bandwidth_Range, int[] Average_Arrival_Rate_Range, int[] Num_Node_Range, int[] Num_Edge_Range, int[] DAG_Category_Range, int[] Num_Apps_Timeslot_Range, int[] Microservice_Type_CPU, int[] Microservice_Type_Memory, double Lowest_Communication_Latency, double Highest_Communication_Latency, int Lowest_Bandwidth_Capacity, int Highest_Bandwidth_Capacity, int Lowest_Microservice_Bandwidth_Requirement, int Highest_Microservice_Bandwidth_Requirement, int Lowest_Microservice_Type_Unit_Process_Ability, int Highest_Microservice_Type_Unit_Process_Ability) {
        this.Num_Server = Num_Server;
        this.Num_Microservice = Num_Microservice;
        this.serviceTypeInfos = serviceTypeInfos;
        this.Num_Application = Num_Application;
        this.Num_Time_Slot = Num_Time_Slot;
        this.Num_CPU_Core = Num_CPU_Core;
        this.MAX1 = MAX1;
        this.Arrival_Time_Range = Arrival_Time_Range;
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

    // Getters 和 Setters 方法
    public int getNum_Server() {
        return Num_Server;
    }

    public void setNum_Server(int num_Server) {
        Num_Server = num_Server;
    }

    public int getNum_Microservice() {
        return Num_Microservice;
    }

    public void setNum_Microservice(int num_Microservice) {
        Num_Microservice = num_Microservice;
    }

    public int getNum_Application() {
        return Num_Application;
    }

    public void setNum_Application(int num_Application) {
        Num_Application = num_Application;
    }

    public int getNum_Time_Slot() {
        return Num_Time_Slot;
    }

    public void setNum_Time_Slot(int num_Time_Slot) {
        Num_Time_Slot = num_Time_Slot;
    }

    public int getNum_CPU_Core() {
        return Num_CPU_Core;
    }

    public void setNum_CPU_Core(int num_CPU_Core) {
        Num_CPU_Core = num_CPU_Core;
    }

    public int getMAX1() {
        return MAX1;
    }

    public void setMAX1(int MAX1) {
        this.MAX1 = MAX1;
    }

    public int[] getArrival_Time_Range() {
        return Arrival_Time_Range;
    }

    public void setArrival_Time_Range(int[] arrival_Time_Range) {
        Arrival_Time_Range = arrival_Time_Range;
    }

    public int[] getTTL_Max_Tolerance_Latency_Range() {
        return TTL_Max_Tolerance_Latency_Range;
    }

    public void setTTL_Max_Tolerance_Latency_Range(int[] TTL_Max_Tolerance_Latency_Range) {
        this.TTL_Max_Tolerance_Latency_Range = TTL_Max_Tolerance_Latency_Range;
    }

    public double[] getUnit_Rate_Bandwidth_Range() {
        return Unit_Rate_Bandwidth_Range;
    }

    public void setUnit_Rate_Bandwidth_Range(double[] unit_Rate_Bandwidth_Range) {
        Unit_Rate_Bandwidth_Range = unit_Rate_Bandwidth_Range;
    }

    public int[] getAverage_Arrival_Rate_Range() {
        return Average_Arrival_Rate_Range;
    }

    public void setAverage_Arrival_Rate_Range(int[] average_Arrival_Rate_Range) {
        Average_Arrival_Rate_Range = average_Arrival_Rate_Range;
    }

    public int[] getNum_Node_Range() {
        return Num_Node_Range;
    }

    public void setNum_Node_Range(int[] num_Node_Range) {
        Num_Node_Range = num_Node_Range;
    }

    public int[] getNum_Edge_Range() {
        return Num_Edge_Range;
    }

    public void setNum_Edge_Range(int[] num_Edge_Range) {
        Num_Edge_Range = num_Edge_Range;
    }

    public int[] getDAG_Category_Range() {
        return DAG_Category_Range;
    }

    public void setDAG_Category_Range(int[] DAG_Category_Range) {
        this.DAG_Category_Range = DAG_Category_Range;
    }

    public int[] getNum_Apps_Timeslot_Range() {
        return Num_Apps_Timeslot_Range;
    }

    public void setNum_Apps_Timeslot_Range(int[] num_Apps_Timeslot_Range) {
        Num_Apps_Timeslot_Range = num_Apps_Timeslot_Range;
    }

    public int[] getMicroservice_Type_CPU() {
        return Microservice_Type_CPU;
    }

    public void setMicroservice_Type_CPU(int[] microservice_Type_CPU) {
        Microservice_Type_CPU = microservice_Type_CPU;
    }

    public int[] getMicroservice_Type_Memory() {
        return Microservice_Type_Memory;
    }

    public void setMicroservice_Type_Memory(int[] microservice_Type_Memory) {
        Microservice_Type_Memory = microservice_Type_Memory;
    }

    public double getLowest_Communication_Latency() {
        return Lowest_Communication_Latency;
    }

    public void setLowest_Communication_Latency(double lowest_Communication_Latency) {
        Lowest_Communication_Latency = lowest_Communication_Latency;
    }

    public double getHighest_Communication_Latency() {
        return Highest_Communication_Latency;
    }

    public void setHighest_Communication_Latency(double highest_Communication_Latency) {
        Highest_Communication_Latency = highest_Communication_Latency;
    }

    public int getLowest_Bandwidth_Capacity() {
        return Lowest_Bandwidth_Capacity;
    }

    public void setLowest_Bandwidth_Capacity(int lowest_Bandwidth_Capacity) {
        Lowest_Bandwidth_Capacity = lowest_Bandwidth_Capacity;
    }

    public int getHighest_Bandwidth_Capacity() {
        return Highest_Bandwidth_Capacity;
    }

    public void setHighest_Bandwidth_Capacity(int highest_Bandwidth_Capacity) {
        Highest_Bandwidth_Capacity = highest_Bandwidth_Capacity;
    }

    public int getLowest_Microservice_Bandwidth_Requirement() {
        return Lowest_Microservice_Bandwidth_Requirement;
    }

    public void setLowest_Microservice_Bandwidth_Requirement(int lowest_Microservice_Bandwidth_Requirement) {
        Lowest_Microservice_Bandwidth_Requirement = lowest_Microservice_Bandwidth_Requirement;
    }

    public int getHighest_Microservice_Bandwidth_Requirement() {
        return Highest_Microservice_Bandwidth_Requirement;
    }

    public void setHighest_Microservice_Bandwidth_Requirement(int highest_Microservice_Bandwidth_Requirement) {
        Highest_Microservice_Bandwidth_Requirement = highest_Microservice_Bandwidth_Requirement;
    }

    public int getLowest_Microservice_Type_Unit_Process_Ability() {
        return Lowest_Microservice_Type_Unit_Process_Ability;
    }

    public void setLowest_Microservice_Type_Unit_Process_Ability(int lowest_Microservice_Type_Unit_Process_Ability) {
        Lowest_Microservice_Type_Unit_Process_Ability = lowest_Microservice_Type_Unit_Process_Ability;
    }

    public int getHighest_Microservice_Type_Unit_Process_Ability() {
        return Highest_Microservice_Type_Unit_Process_Ability;
    }

    public void setHighest_Microservice_Type_Unit_Process_Ability(int highest_Microservice_Type_Unit_Process_Ability) {
        Highest_Microservice_Type_Unit_Process_Ability = highest_Microservice_Type_Unit_Process_Ability;
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

    public String toString() {
        return "App_Params{Num_Server = " + Num_Server + ", Num_Microservice = " + Num_Microservice + ", serviceTypeInfos = " + serviceTypeInfos + ", Num_Application = " + Num_Application + ", Num_Time_Slot = " + Num_Time_Slot + ", Num_CPU_Core = " + Num_CPU_Core + ", MAX1 = " + MAX1 + ", Arrival_Time_Range = " + Arrival_Time_Range + ", TTL_Max_Tolerance_Latency_Range = " + TTL_Max_Tolerance_Latency_Range + ", Unit_Rate_Bandwidth_Range = " + Unit_Rate_Bandwidth_Range + ", Average_Arrival_Rate_Range = " + Average_Arrival_Rate_Range + ", Num_Node_Range = " + Num_Node_Range + ", Num_Edge_Range = " + Num_Edge_Range + ", DAG_Category_Range = " + DAG_Category_Range + ", Num_Apps_Timeslot_Range = " + Num_Apps_Timeslot_Range + ", Microservice_Type_CPU = " + Microservice_Type_CPU + ", Microservice_Type_Memory = " + Microservice_Type_Memory + ", Lowest_Communication_Latency = " + Lowest_Communication_Latency + ", Highest_Communication_Latency = " + Highest_Communication_Latency + ", Lowest_Bandwidth_Capacity = " + Lowest_Bandwidth_Capacity + ", Highest_Bandwidth_Capacity = " + Highest_Bandwidth_Capacity + ", Lowest_Microservice_Bandwidth_Requirement = " + Lowest_Microservice_Bandwidth_Requirement + ", Highest_Microservice_Bandwidth_Requirement = " + Highest_Microservice_Bandwidth_Requirement + ", Lowest_Microservice_Type_Unit_Process_Ability = " + Lowest_Microservice_Type_Unit_Process_Ability + ", Highest_Microservice_Type_Unit_Process_Ability = " + Highest_Microservice_Type_Unit_Process_Ability + "}";
    }
}
