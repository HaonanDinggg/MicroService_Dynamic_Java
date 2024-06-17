package utils;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-17 16:34
 **/
public class Connection {
    private double bandwidth; // 带宽
    private double delay; // 单位请求流通信时延

    public Connection(double bandwidth, double delay) {
        this.bandwidth = bandwidth;
        this.delay = delay;
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
}
