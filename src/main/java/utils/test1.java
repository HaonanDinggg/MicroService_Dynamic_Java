package utils;

import java.util.*;

public class test1 {

    public static void main(String[] args) {
        // 示例输入数据
        List<List<Object>> migrationList = new ArrayList<>();
        migrationList.add(Arrays.asList(1, 1, 2, 2, 0.5));
        migrationList.add(Arrays.asList(1, 1, 2, 2, 0.3));
        migrationList.add(Arrays.asList(1, 2, 3, 4, 0.4));

        // 合并迁移列表
        List<List<Object>> mergedList = mergeMigrationList(migrationList);

        // 输出结果
        for (List<Object> migrationPair : mergedList) {
            System.out.println(migrationPair);
        }
    }

    public static List<List<Object>> mergeMigrationList(List<List<Object>> migrationList) {
        // 使用Map来合并具有相同起点和终点的拓扑对
        Map<String, List<Object>> mergedMap = new HashMap<>();

        for (List<Object> migrationPair : migrationList) {
            int startMicroservice = (int) migrationPair.get(0);
            int startNode = (int) migrationPair.get(1);
            int endMicroservice = (int) migrationPair.get(2);
            int endNode = (int) migrationPair.get(3);
            double arrivalRate = (double) migrationPair.get(4);

            String key = startMicroservice + "-" + startNode + "-" + endMicroservice + "-" + endNode;

            if (mergedMap.containsKey(key)) {
                List<Object> existingPair = mergedMap.get(key);
                double existingArrivalRate = (double) existingPair.get(4);
                existingPair.set(4, existingArrivalRate + arrivalRate);
            } else {
                mergedMap.put(key, new ArrayList<>(migrationPair));
            }
        }

        // 将合并后的结果转换回List<List<Object>>
        return new ArrayList<>(mergedMap.values());
    }

}