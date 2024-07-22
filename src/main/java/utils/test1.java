package utils;

import java.util.*;

public class test1 {

    public static void main(String[] args) {
        // 示例数据
        int[][] InstanceDeployOnNode = {
                {5, 2, 0},
                {3, 0, 4},
                {0, 1, 6}
        };

        List<Integer> serviceChain = Arrays.asList(0, 1, 2);
        double arrivalRate = 100.0;

        // 计算所有可能的路由路径及其概率
        List<Map<Integer, Double>> routes = calculateRoutes(InstanceDeployOnNode, serviceChain, arrivalRate);

        // 打印结果
        for (Map<Integer, Double> route : routes) {
            System.out.println(route);
        }
    }

    public static List<Map<Integer, Double>> calculateRoutes(int[][] instanceDeployOnNode, List<Integer> serviceChain, double arrivalRate) {
        List<Map<Integer, Double>> allRoutes = new ArrayList<>();
        Map<Integer, Double> currentRoute = new HashMap<>();
        calculateRoutesRecursive(instanceDeployOnNode, serviceChain, arrivalRate, 0, currentRoute, allRoutes);
        return allRoutes;
    }

    private static void calculateRoutesRecursive(int[][] instanceDeployOnNode, List<Integer> serviceChain, double arrivalRate, int serviceIndex, Map<Integer, Double> currentRoute, List<Map<Integer, Double>> allRoutes) {
        if (serviceIndex == serviceChain.size()) {
            allRoutes.add(new HashMap<>(currentRoute));
            return;
        }

        int serviceId = serviceChain.get(serviceIndex);
        double totalInstances = 0;
        for (int[] nodeInstances : instanceDeployOnNode) {
            totalInstances += nodeInstances[serviceId];
        }

        if (totalInstances == 0) {
            return; // 如果当前服务没有任何实例，直接返回
        }

        for (int nodeId = 0; nodeId < instanceDeployOnNode.length; nodeId++) {
            int instances = instanceDeployOnNode[nodeId][serviceId];
            if (instances > 0) {
                double probability = instances / totalInstances;
                double addedLoad = arrivalRate * probability;
                currentRoute.put(nodeId, currentRoute.getOrDefault(nodeId, 0.0) + addedLoad);
                calculateRoutesRecursive(instanceDeployOnNode, serviceChain, arrivalRate * probability, serviceIndex + 1, currentRoute, allRoutes);
                currentRoute.put(nodeId, currentRoute.get(nodeId) - addedLoad);
            }
        }
    }
}