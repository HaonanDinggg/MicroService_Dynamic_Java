package utils;

import java.util.*;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-08 11:23
 **/
public class test {
    public static void main(String[] args) {
        int roundRobinParam = 3;
        ArrayList<Integer> currentServiceSplitInstance = new ArrayList<>(Collections.nCopies(roundRobinParam, 0));
        int instanceNum = 7;
        for (int instance = 0; instance < instanceNum; instance++) {
            int queueIndex = instance % roundRobinParam;
            System.out.println("queueIndex"+queueIndex);
            if (instance + 1 <= roundRobinParam) {
                currentServiceSplitInstance.set(queueIndex, 1);
            } else {
                currentServiceSplitInstance.set(queueIndex, (int) (instance / roundRobinParam) + 1);
            }
        }
        System.out.println(currentServiceSplitInstance);
        // 示例数据
        Map<Integer, ArrayList<Integer>> splitInstances = new HashMap<>();
        ArrayList<Integer> service1Queues = new ArrayList<>();
        service1Queues.add(2);
        service1Queues.add(3);
        splitInstances.put(1, service1Queues);

        ArrayList<Integer> service2Queues = new ArrayList<>();
        service2Queues.add(1);
        service2Queues.add(4);
        service2Queues.add(5);
        splitInstances.put(2, service2Queues);

        ArrayList<Integer> service3Queues = new ArrayList<>();
        service3Queues.add(5);
        service3Queues.add(5);
        splitInstances.put(3, service3Queues);

        List<PathInfo> pathInfos = calculatePathInfos(splitInstances);

        // 输出所有路径及其概率和时延
        for (PathInfo pi : pathInfos) {
            System.out.println("Path: " + pi.path + ", Probability: " + pi.probability + ", Latency: " + pi.latency);
        }
    }
    public static List<PathInfo> calculatePathInfos(Map<Integer, ArrayList<Integer>> splitInstances) {
        List<PathInfo> results = new ArrayList<>();
        List<Integer> currentPath = new ArrayList<>();
        calculatePathInfosRecursive(splitInstances, results, currentPath, 1.0, 0, 0.0);
        return results;
    }

    private static void calculatePathInfosRecursive(Map<Integer, ArrayList<Integer>> splitInstances, List<PathInfo> results,
                                                    List<Integer> currentPath, double currentProbability, int serviceIndex, double currentLatency) {
        if (serviceIndex >= splitInstances.size()) {
            results.add(new PathInfo(new ArrayList<>(currentPath), currentProbability, currentLatency));
            return;
        }

        int serviceId = serviceIndex + 1;
        ArrayList<Integer> instanceCounts = splitInstances.get(serviceId);
        int totalInstances = instanceCounts.stream().mapToInt(Integer::intValue).sum();

        for (int i = 0; i < instanceCounts.size(); i++) {
            int count = instanceCounts.get(i);
            currentPath.add(i);
            double newLatency = currentLatency + calculateLatency(serviceId, i); // 增加时延计算
            calculatePathInfosRecursive(splitInstances, results, currentPath, currentProbability * ((double) count / totalInstances), serviceIndex + 1, newLatency);
            currentPath.remove(currentPath.size() - 1);
        }
    }

    // 计算特定微服务队列的时延的方法，具体实现需要完善
    private static double calculateLatency(int serviceId, int queueIndex) {
        // TODO: 根据serviceId和queueIndex计算并返回相应的时延
        return 0.0; // 示例返回值
    }

    static class PathInfo {
        List<Integer> path;
        double probability;
        double latency;

        PathInfo(List<Integer> path, double probability, double latency) {
            this.path = path;
            this.probability = probability;
            this.latency = latency;
        }
    }

}
