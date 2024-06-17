package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MicroserviceRouting {

    public static void main(String[] args) {
        // 定义三维数组，50条请求流，每个请求流访问50个节点上的10种微服务
        int[][][] deploymentArray = new int[50][10][50];

        // 填充示例数据
        deploymentArray[0][5][0] = 1; // 请求流0在节点1访问1个f实例
        deploymentArray[0][2][0] = 1; // 请求流0在节点1访问1个c实例
        deploymentArray[0][9][0] = 2; // 请求流0在节点1访问2个j实例
        deploymentArray[0][8][0] = 1; // 请求流0在节点1访问1个i实例
        deploymentArray[0][7][0] = 1; // 请求流0在节点1访问1个h实例

        deploymentArray[1][0][0] = 1; // 请求流1在节点1访问1个a实例
        deploymentArray[1][1][0] = 2; // 请求流1在节点1访问2个b实例
        deploymentArray[1][2][0] = 2; // 请求流1在节点1访问2个c实例

        deploymentArray[2][0][0] = 1; // 请求流2在节点1访问1个a实例
        deploymentArray[2][3][0] = 2; // 请求流2在节点1访问2个d实例
        deploymentArray[2][2][0] = 2; // 请求流2在节点1访问2个c实例

        // 微服务类型
        String[] microservices = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

        // 构建请求流的微服务顺序
        ArrayList<ArrayList<Integer>> K8s_Req_Stream = new ArrayList<>();

        // 示例请求流
        ArrayList<Integer> single_req_flow_topology1 = new ArrayList<>();
        single_req_flow_topology1.add(5); // f
        single_req_flow_topology1.add(2); // c
        single_req_flow_topology1.add(9); // j
        single_req_flow_topology1.add(8); // i
        single_req_flow_topology1.add(7); // h
        K8s_Req_Stream.add(single_req_flow_topology1);

        ArrayList<Integer> single_req_flow_topology2 = new ArrayList<>();
        single_req_flow_topology2.add(0); // a
        single_req_flow_topology2.add(1); // b
        single_req_flow_topology2.add(2); // c
        K8s_Req_Stream.add(single_req_flow_topology2);

        ArrayList<Integer> single_req_flow_topology3 = new ArrayList<>();
        single_req_flow_topology3.add(0); // a
        single_req_flow_topology3.add(3); // d
        single_req_flow_topology3.add(2); // c
        K8s_Req_Stream.add(single_req_flow_topology3);

        // 实例编号初始化
        Map<Integer, Integer> globalServiceCount = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            globalServiceCount.put(i, 0);
        }

        // 遍历每条请求流
        for (int reqIndex = 0; reqIndex < K8s_Req_Stream.size(); reqIndex++) {
            ArrayList<Integer> reqFlow = K8s_Req_Stream.get(reqIndex);
            ArrayList<String> result = new ArrayList<>();
            Map<Integer, ArrayList<Integer>> instanceMap = new HashMap<>();

            // 获取每个微服务的实例编号
            for (int microIndex : reqFlow) {
                int instanceCount = getTotalInstances(deploymentArray, reqIndex, microIndex);
                ArrayList<Integer> instances = new ArrayList<>();
                for (int i = 0; i < instanceCount; i++) {
                    instances.add(globalServiceCount.get(microIndex) + 1 + i);
                }
                instanceMap.put(microIndex, instances);
                globalServiceCount.put(microIndex, globalServiceCount.get(microIndex) + instanceCount);
            }

            // 生成所有可能的组合
            generateCombinations(reqFlow, 0, new int[reqFlow.size()], instanceMap, result, microservices);
            for (String route : result) {
                System.out.println(route);
            }
        }
    }

    private static void generateCombinations(ArrayList<Integer> reqFlow, int depth, int[] currentCombination, Map<Integer, ArrayList<Integer>> instanceMap, ArrayList<String> result, String[] microservices) {
        if (depth == reqFlow.size()) {
            // 已生成一个完整的组合
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentCombination.length; i++) {
                sb.append(microservices[reqFlow.get(i)]).append(currentCombination[i]);
                if (i < currentCombination.length - 1) {
                    sb.append(",");
                }
            }
            result.add(sb.toString());
            return;
        }

        int microIndex = reqFlow.get(depth);
        ArrayList<Integer> instances = instanceMap.get(microIndex);

        for (int instance : instances) {
            currentCombination[depth] = instance;
            generateCombinations(reqFlow, depth + 1, currentCombination, instanceMap, result, microservices);
        }
    }

    private static int getTotalInstances(int[][][] deploymentArray, int reqIndex, int microIndex) {
        int total = 0;
        for (int i = 0; i < deploymentArray[reqIndex][microIndex].length; i++) {
            total += deploymentArray[reqIndex][microIndex][i];
        }
        return total;
    }
}
