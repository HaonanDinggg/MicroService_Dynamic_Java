package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-17 15:40
 **/
public class ServiceTypeInfo {
    private int ServiceID; //微服务ID
    private int ServiceState; //判断微服务种类，是否为有状态的
    private int ServiceCPU; //单位实例cpu资源占用
    private int ServiceMemory; //单位实例存储资源占用
    private int ServiceProcessingRate;//单位实例微服务处理能力

    public ServiceTypeInfo() {
    }


    public ServiceTypeInfo(int ServiceID, int ServiceState, int ServiceProcessingRate) {
        this.ServiceID = ServiceID;
        this.ServiceState = ServiceState;
        this.ServiceProcessingRate = ServiceProcessingRate;
    }

    public ServiceTypeInfo(int ServiceID, int ServiceState, int ServiceCPU, int ServiceMemory, int ServiceProcessingRate) {
        this.ServiceID = ServiceID;
        this.ServiceState = ServiceState;
        this.ServiceCPU = ServiceCPU;
        this.ServiceMemory = ServiceMemory;
        this.ServiceProcessingRate = ServiceProcessingRate;
    }


    /**
     * 获取
     * @return ServiceID
     */
    public int getServiceID() {
        return ServiceID;
    }

    /**
     * 设置
     * @param ServiceID
     */
    public void setServiceID(int ServiceID) {
        this.ServiceID = ServiceID;
    }

    /**
     * 获取
     * @return ServiceState
     */
    public int getServiceState() {
        return ServiceState;
    }

    /**
     * 设置
     * @param ServiceState
     */
    public void setServiceState(int ServiceState) {
        this.ServiceState = ServiceState;
    }


    /**
     * 获取
     * @return ServiceProcessingRate
     */
    public int getServiceProcessingRate() {
        return ServiceProcessingRate;
    }

    /**
     * 设置
     * @param ServiceProcessingRate
     */
    public void setServiceProcessingRate(int ServiceProcessingRate) {
        this.ServiceProcessingRate = ServiceProcessingRate;
    }

    /**
     * 获取
     * @return ServiceCPU
     */
    public int getServiceCPU() {
        return ServiceCPU;
    }

    /**
     * 设置
     * @param ServiceCPU
     */
    public void setServiceCPU(int ServiceCPU) {
        this.ServiceCPU = ServiceCPU;
    }

    /**
     * 获取
     * @return ServiceMemory
     */
    public int getServiceMemory() {
        return ServiceMemory;
    }

    /**
     * 设置
     * @param ServiceMemory
     */
    public void setServiceMemory(int ServiceMemory) {
        this.ServiceMemory = ServiceMemory;
    }


    public String toString() {
        return "ServiceTypeInfo{ServiceID = " + ServiceID + ", ServiceState = " + ServiceState + ", ServiceCPU = " + ServiceCPU + ", ServiceMemory = " + ServiceMemory + ", ServiceProcessingRate = " + ServiceProcessingRate + "}";
    }
}
