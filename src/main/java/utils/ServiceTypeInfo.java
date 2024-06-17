package utils;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-17 15:40
 **/
public class ServiceTypeInfo {
    private int ServiceID; //微服务ID
    private int ServiceState; //判断微服务种类，是否为有状态的
    private int ServiceProcessingRate;

    public ServiceTypeInfo() {
    }

    public ServiceTypeInfo(int ServiceID, int ServiceState) {
        this.ServiceID = ServiceID;
        this.ServiceState = ServiceState;
    }

    public ServiceTypeInfo(int ServiceID, int ServiceState, int ServiceProcessingRate) {
        this.ServiceID = ServiceID;
        this.ServiceState = ServiceState;
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

    public String toString() {
        return "ServiceTypeInfo{ServiceID = " + ServiceID + ", ServiceState = " + ServiceState + "}";
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
}
