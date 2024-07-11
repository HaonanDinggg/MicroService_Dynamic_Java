package utils;

import java.util.List;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-08 15:22
 **/
public class TempPathInfo {
    List<Integer> path;
    double probability;
    double latency;

    TempPathInfo(List<Integer> path, double probability, double latency) {
        this.path = path;
        this.probability = probability;
        this.latency = latency;
    }
}
