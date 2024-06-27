package utils;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-17 16:34
 **/
public class Connection {
    private double bandwidth; // 带宽
    private double delay; // 单位请求流到达率/带宽获得的请求流通信时延

    public Connection(double bandwidth, double delay) {
        this.bandwidth = bandwidth;
        this.delay = delay;
    }

    public Connection() {
    }

    public double getBandwidth() {
        return bandwidth;
    }

    public double getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "bandwidth=" + bandwidth +
                ", delay=" + delay +
                '}';
    }

    /**
     * 设置
     * @param bandwidth
     */
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * 设置
     * @param delay
     */
    public void setDelay(double delay) {
        this.delay = delay;
    }
}
