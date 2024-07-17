package utils;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-07-16 14:46
 **/
import java.util.HashMap;
import java.util.Map;

public class test1 {

    public static void main(String[] args) {
        // 示例数据
        Map<Integer, Map<Integer, Integer>> backwardServiceInstanceNum = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> forwardServiceInstanceNum = new HashMap<>();

        // 初始化示例数据
        initializeExampleData(backwardServiceInstanceNum, forwardServiceInstanceNum);

        // 计算合计的微服务实例数
        Map<Integer, Integer> totalServiceInstances = calculateTotalServiceInstances(backwardServiceInstanceNum, forwardServiceInstanceNum);

        // 打印结果
        System.out.println("Total service instances per node: " + totalServiceInstances);
    }

    private static void initializeExampleData(Map<Integer, Map<Integer, Integer>> backwardServiceInstanceNum, Map<Integer, Map<Integer, Integer>> forwardServiceInstanceNum) {
        // 初始化backwardServiceInstanceNum
        Map<Integer, Integer> node1BackwardServices = new HashMap<>();
        node1BackwardServices.put(1, 2); // 微服务ID 1，实例数 2
        node1BackwardServices.put(2, 3); // 微服务ID 2，实例数 3
        backwardServiceInstanceNum.put(101, node1BackwardServices); // 节点ID 101

        Map<Integer, Integer> node2BackwardServices = new HashMap<>();
        node2BackwardServices.put(1, 1); // 微服务ID 1，实例数 1
        node2BackwardServices.put(3, 4); // 微服务ID 3，实例数 4
        backwardServiceInstanceNum.put(102, node2BackwardServices); // 节点ID 102

        // 初始化forwardServiceInstanceNum
        Map<Integer, Integer> node1ForwardServices = new HashMap<>();
        node1ForwardServices.put(1, 1); // 微服务ID 1，实例数 1
        node1ForwardServices.put(4, 5); // 微服务ID 4，实例数 5
        forwardServiceInstanceNum.put(101, node1ForwardServices); // 节点ID 101

        Map<Integer, Integer> node3ForwardServices = new HashMap<>();
        node3ForwardServices.put(2, 2); // 微服务ID 2，实例数 2
        node3ForwardServices.put(3, 3); // 微服务ID 3，实例数 3
        forwardServiceInstanceNum.put(103, node3ForwardServices); // 节点ID 103
    }

    private static Map<Integer, Integer> calculateTotalServiceInstances(Map<Integer, Map<Integer, Integer>> backwardServiceInstanceNum, Map<Integer, Map<Integer, Integer>> forwardServiceInstanceNum) {
        Map<Integer, Integer> totalServiceInstances = new HashMap<>();

        // 合计backwardServiceInstanceNum中的实例数
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : backwardServiceInstanceNum.entrySet()) {
            int nodeId = entry.getKey();
            Map<Integer, Integer> services = entry.getValue();
            int totalInstances = services.values().stream().mapToInt(Integer::intValue).sum();
            totalServiceInstances.put(nodeId, totalInstances);
        }

        // 合计forwardServiceInstanceNum中的实例数
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : forwardServiceInstanceNum.entrySet()) {
            int nodeId = entry.getKey();
            Map<Integer, Integer> services = entry.getValue();
            int totalInstances = services.values().stream().mapToInt(Integer::intValue).sum();
            totalServiceInstances.merge(nodeId, totalInstances, Integer::sum);
        }

        return totalServiceInstances;
    }
}
