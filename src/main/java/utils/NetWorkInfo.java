package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: Dior
 * @Desc:用于存储节点间的传输信息
 * @create: 2024-06-17 16:34
 **/
public class NetWorkInfo {
    private Map<PhysicalNodeInfo, Map<PhysicalNodeInfo, Connection>> connections = new HashMap<>();

    public void addNode(PhysicalNodeInfo node) {
        connections.putIfAbsent(node, new HashMap<>());
    }

    public void addConnection(PhysicalNodeInfo node1, PhysicalNodeInfo node2, double bandwidth, double delay) {
        connections.get(node1).put(node2, new Connection(bandwidth, delay));
        connections.get(node2).put(node1, new Connection(bandwidth, delay));
    }

    public Connection getConnection(PhysicalNodeInfo node1, PhysicalNodeInfo node2) {
        return connections.get(node1).get(node2);
    }

    public Set<PhysicalNodeInfo> getNodes() {
        return connections.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PhysicalNodeInfo node : connections.keySet()) {
            sb.append(node).append(" connections:\n");
            for (Map.Entry<PhysicalNodeInfo, Connection> entry : connections.get(node).entrySet()) {
                sb.append("  to ").append(entry.getKey())
                        .append(" -> ").append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
    public static void main(String[] args) {
        NetWorkInfo network = new NetWorkInfo();

        PhysicalNodeInfo nodeA = new PhysicalNodeInfo(0);
        PhysicalNodeInfo nodeB = new PhysicalNodeInfo(1);
        PhysicalNodeInfo nodeC = new PhysicalNodeInfo(2);

        network.addNode(nodeA);
        network.addNode(nodeB);
        network.addNode(nodeC);

        network.addConnection(nodeA, nodeB, 100.0, 10.0);
        network.addConnection(nodeA, nodeC, 150.0, 15.0);
        network.addConnection(nodeB, nodeC, 200.0, 20.0);

        System.out.println(network);

        Connection connection = network.getConnection(nodeA, nodeB);
        System.out.println("Connection between A and B: " + connection);
    }
}
