package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.xml.internal.bind.v2.TODO;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author: Dior
 * @Desc:
 * @create: 2024-06-13 14:58
 **/

public class App_Creator {
    //    private static int[] indegree;
//    // 记录已经遍历的节点
//    private static boolean[] visited;
//    // 记录拓扑排序的结果
//    private static List<Integer> result;
    static Random random = new Random(214);
    public static void main(String[] args) {
        int timeplot = 5;
        App_Params appParams = new App_Params();
        appParams.setNum_Microservice(10);
        appParams.CreateServiceList();
        List<ServiceTypeInfo> serviceTypeInfos = appParams.getServiceTypeInfos();
        //需要初始化所有的ServiceTypeInfo 共用一套微服务信息
        for(int t = 0; t < timeplot ;t++) {
            int AppNum = 10;
            for(int Num = 0; Num < AppNum ; Num++) {
                AppPathInfo dagPathInfo1 = new AppPathInfo();
                dagPathInfo1.setNum_Dag_Edge(4);
                dagPathInfo1.setNum_Dag_Node(5);
                dagPathInfo1.setAppType(1);
                dagPathInfo1.setNum_MicroService(appParams.getNum_Microservice());

                dagPathInfo1 = generateDAGPathInfo(dagPathInfo1, serviceTypeInfos);
                try {
                    saveToJsonFile(dagPathInfo1, "D:\\华科工作\\实验室工作\\胡毅学长动态\\Dynamic_Java\\daginfos\\dagPathInfo.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(dagPathInfo1);
            }
        }
    }

//    /*
//      * @Description : 生成对应拓扑排序
//      * @Author : THINK
//      *  * @param node
// * @param DAG
//      * @return : java.util.ArrayList<java.lang.Integer>
//      * @Date : 2024/6/13
//      * @Version : 1.0
//      * @Copyright : © 2024 All Rights Reserved.
//      **/
//    public static ArrayList<Integer> Get_single_req_flow_topology(int node,int[][] DAG) {
//        ArrayList<Integer> single_req_flow_topology = new ArrayList<>();
//        Random random = new Random(214);
//        List<Integer> topology = topologicalSort(DAG).subList(0, node);//此方法根据DAG生成拓扑排序
//        Map<Integer, Integer> single_req_flow = new LinkedHashMap<>();
//        for(int i = 0;i < node  ;i++){
//            int temp=random.nextInt(10);//随机生产某一node上的某一微服务 将两者关联起来
//            int NodeIndex = topology.get(i);//获取拓扑排序第j个节点的索引
//
//            if(i==0){
//                single_req_flow.put(NodeIndex, temp);//添加对应DAG的节点索引以及对应的微服务编号
//                single_req_flow_topology.add(temp);//根据拓扑排序的顺序添加微服务序号 我们通过上面的single_req_flow获取对应的该索引编号的DAG图的节点索引
//
//                // single_req_flow.get(index)可以获取对应map
//                continue;
//            }
//            if (i!=0 && single_req_flow.containsValue(temp)){//保证一个服务请求中每种微服务只出现一次 重复则该循环重复一次
//                i--;
//            }else {
//                single_req_flow.put(NodeIndex, temp);
//                single_req_flow_topology.add(temp);
//            }
//        }
//        return single_req_flow_topology;
//    }

    /*
     * @Description : 随机生成DAG图
     * @Author : Dior
     *  * @param node
     * @param edge
     * @return : int[][]
     * @Date : 2024/6/13
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static int[][] randomGraph(int node, int edge) {

        int n = node;
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes);  // 生成拓扑排序

        int m = edge;
        List<int[]> result = new ArrayList<>(); // 存储生成的边，边用int数组的形式存储

        List<Integer> appearedNodes = new ArrayList<>();
        List<Integer> notAppearedNodes = new ArrayList<>(nodes);
        // 生成前n - 1条边
        while (result.size() != n - 1) {
            // 生成第一条边
            if (result.isEmpty()) {
                int p1 = new Random().nextInt(n - 1);
                int p2 = new Random().nextInt(n - p1 - 1) + p1 + 1;;
                int x = nodes.get(p1);
                int y = nodes.get(p2);
                appearedNodes.add(x);
                appearedNodes.add(y);
                notAppearedNodes.removeAll(appearedNodes);
                result.add(new int[] {x, y});
            }
            // 生成后面的边
            else {
                int p1 = new Random().nextInt(appearedNodes.size());
                int x = appearedNodes.get(p1); // 第一个点从已经出现的点中选择
                int p2 = new Random().nextInt(notAppearedNodes.size());
                int y = notAppearedNodes.get(p2);
                appearedNodes.add(y); // 第二个点从没有出现的点中选择
                notAppearedNodes.removeAll(appearedNodes);
                // 必须保证第一个点的排序在第二个点之前
                if (nodes.indexOf(y) < nodes.indexOf(x)) {
                    result.add(new int[] {y, x});
                } else {
                    result.add(new int[] {x, y});
                }
            }
        }
        // 生成后m - n + 1条边
        while (result.size() != m) {
            int p1 = new Random().nextInt(n - 1);
            int p2 = new Random().nextInt(n - p1 - 1) + p1 + 1;;
            int x = nodes.get(p1);
            int y = nodes.get(p2);
            // 如果该条边已经生成过，则重新生成
            if (containsEdge(result, x, y)) {
                continue;
            } else {
                result.add(new int[] {x, y});
            }
        }

        int[][] matrix = new int[n][n];
        for (int[] edgeArr : result) {
            matrix[edgeArr[0]][edgeArr[1]] = 1;
        }

        return matrix;
    }
    /*
     * @Description : 判断边是否已经生成过
     * @Author : Dior
     *  * @param edges
     * @param x
     * @param y
     * @return : boolean
     * @Date : 2024/6/13
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    private static boolean containsEdge(List<int[]> edges, int x, int y) {
        for (int[] edgeArr : edges) {
            if (edgeArr[0] == x && edgeArr[1] == y) {
                return true;
            }
        }
        return false;
    }
    /*
     * @Description : 生成DAG图 保证一个入度为0的节点只有一个
     * @Author : Dior
     *  * @param node
     * @param edge
     * @return : int[][]
     * @Date : 2024/6/13
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static int[][] CorrectGraph(int node, int edge){
        int colulnflag = 2;//存储全为0的列数 为进入循环则初始为2
        int rowflag = 2;//存储全为0的行数
        boolean colulnjudge = true;
        boolean rowjudge = true;
        int[][] adjMatrix= randomGraph(node, edge);
        while(colulnflag > 1 || rowflag > 1) {
            //如果出现两个及以上起点或者终点 则判断该图不成立 则重新生成
            adjMatrix= randomGraph(node, edge);
            colulnflag = 0;
            rowflag = 0;
            colulnjudge = true;//每次判断重置flag
            rowjudge = true;//每次判断重置flag
            for(int i = 0 ; i < node ; i ++) {
                for(int j = 0 ; j < node ; j ++) {
                    if(adjMatrix[j][i] == 1) {
                        colulnjudge = false;//如果在某一列出现一个1则表示不为起点 就是对入度的判断
                    }
                }
                if(colulnjudge) {
                    colulnflag++;//出现一行全0则表示出现一个起点
                }
                colulnjudge = true;//每次判断重置flag
            }
        }
        return adjMatrix;
    }

//    public static List<Integer> topologicalSort(int[][] adjacencyMatrix) {
//        int n = adjacencyMatrix.length;
//        indegree = new int[n];
//        visited = new boolean[n];
//        result = new ArrayList<>();
//
//        // 统计每个节点的入度
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                if (adjacencyMatrix[i][j] == 1) {
//                    indegree[j]++;
//                }
//            }
//        }
//
//        // 从入度为0的节点开始遍历
//        for (int i = 0; i < n; i++) {
//            if (indegree[i] == 0) {
//                dfs_topology(i,adjacencyMatrix);
//            }
//        }
//
//        return result;
//    }
//
//    //dfs遍历
//    private static void dfs_topology(int i,int[][] adjacencyMatrix) {
//        visited[i] = true;
//        result.add(i);
//        for (int j = 0; j < adjacencyMatrix.length; j++) {
//            if (adjacencyMatrix[i][j] == 1 && !visited[j]) {
//                indegree[j]--;
//                if (indegree[j] == 0) {
//                    dfs_topology(j,adjacencyMatrix);
//                }
//            }
//        }
//    }

    /*
     * @Description : 生成特定APP的DAG图的信息 包含了此图的各个节点信息包含了节点的部署的微服务以及后继节点的概率
     * @Author : Dior
     *  * @param adjMatrix
     * @param Num_Microservice
     * @return : utils.DAGPathInfo
     * @Date : 2024/6/13
     * @Version : 1.0
     * @Copyright : © 2024 All Rights Reserved.
     **/
    public static AppPathInfo generateDAGPathInfo(AppPathInfo dagPathInfo,List<ServiceTypeInfo> serviceTypeInfos) {
        int[][] adjMatrix = CorrectGraph(dagPathInfo.getNum_Dag_Node(),dagPathInfo.getNum_Dag_Edge());
        dagPathInfo.setAdjMatrix(adjMatrix);
        for (int i = 0; i < dagPathInfo.getAdjMatrix().length; i++) {
            System.out.println(Arrays.toString(dagPathInfo.getAdjMatrix()[i]));
        }

        Map<Integer, Integer> serviceTypes = assignServiceTypes(dagPathInfo.getAdjMatrix().length, dagPathInfo.getNum_MicroService());
        Map<Integer, Map<Integer, Double>> transitionProbabilities = generateTransitionProbabilities(dagPathInfo.getAdjMatrix(),dagPathInfo.getAppType());
        Map<Integer, NodeInfo> nodeInfos = createNodeInfos(serviceTypes,serviceTypeInfos, transitionProbabilities);
        List<PathProbability> pathProbabilities = generateAllPaths(dagPathInfo.getAdjMatrix(), nodeInfos);
        dagPathInfo.setNodeInfos(nodeInfos);
        dagPathInfo.setPathProbabilities(pathProbabilities);
        return dagPathInfo;
    }

    /*
      * @Description : 匹配DAG图节点上微服务种类
      * @Author : Dior
      *  * @param numNodes
 * @param Num_Microservice
      * @return : java.util.Map<java.lang.Integer,java.lang.Integer>
      * @Date : 2024/6/14 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static Map<Integer, Integer> assignServiceTypes(int numNodes,int Num_Microservice) {
        Map<Integer, Integer> serviceTypes = new HashMap<>();
        Set<Integer> usedServices = new HashSet<>();
        for (int i = 0; i < numNodes; i++) {
            int service;
            do {
                service = random.nextInt(Num_Microservice);
            } while (usedServices.contains(service));
            usedServices.add(service);
            serviceTypes.put(i, service);
        }
        return serviceTypes;
    }

    /*
      * @Description : 生成DAG图每个节点微服务转发到后续微服务的转发概率，为每个节点生成随机转移概率。，如果是概率转发类型的App就是dag图节点微服务到后续节点微服务概率和为1，如果是并行转发，则dag图节点微服务到后续节点微服务概率均为1
      * @Author : Dior
      *  * @param adjMatrix
 * @param dagType
      * @return : java.util.Map<java.lang.Integer,java.util.Map<java.lang.Integer,java.lang.Double>>
      * @Date : 2024/6/14 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static Map<Integer, Map<Integer, Double>> generateTransitionProbabilities(int[][] adjMatrix,int dagType) {
        Map<Integer, Map<Integer, Double>> transitionProbabilities = new HashMap<>();
        if(dagType==0) {
            for (int i = 0; i < adjMatrix.length; i++) {
                Map<Integer, Double> transitions = new HashMap<>();
                int outDegree = 0;
                for (int j = 0; j < adjMatrix.length; j++) {
                    if (adjMatrix[i][j] == 1) {
                        outDegree++;
                    }
                }
                if (outDegree > 0) {
                    double[] probabilities = new double[outDegree];
                    double sum = 0.0;
                    for (int k = 0; k < outDegree; k++) {
                        probabilities[k] = random.nextDouble();
                        sum += probabilities[k];
                    }
                    for (int k = 0; k < outDegree; k++) {
                        probabilities[k] /= sum;
                    }
                    int index = 0;
                    for (int j = 0; j < adjMatrix.length; j++) {
                        if (adjMatrix[i][j] == 1) {
                            transitions.put(j, probabilities[index]);
                            index++;
                        }
                    }
                }
                transitionProbabilities.put(i, transitions);
            }
        } else if (dagType==1) {
            for (int i = 0; i < adjMatrix.length; i++) {
                Map<Integer, Double> transitions = new HashMap<>();
                for (int j = 0; j < adjMatrix.length; j++) {
                    if (adjMatrix[i][j] == 1) {
                        transitions.put(j, 1.0);
                    }
                }
                transitionProbabilities.put(i, transitions);
            }
        }
        return transitionProbabilities;
    }

    /*
      * @Description : 创建 NodeInfo 对象，并填充转移概率。
      * @Author : Dior
      *  * @param serviceTypes
 * @param transitionProbabilities
      * @return : java.util.Map<java.lang.Integer,utils.NodeInfo>
      * @Date : 2024/6/14 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static Map<Integer, NodeInfo> createNodeInfos(Map<Integer, Integer> serviceTypes,List<ServiceTypeInfo> serviceTypeInfos, Map<Integer, Map<Integer, Double>> transitionProbabilities) {
        Map<Integer, NodeInfo> nodeInfos = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : serviceTypes.entrySet()) {
            int node = entry.getKey();
            int serviceType = entry.getValue();
            ServiceTypeInfo serviceTypeInfo = serviceTypeInfos.get(serviceType);
            Map<NodeInfo, Double> transitions = new HashMap<>();
            nodeInfos.put(node, new NodeInfo(serviceType,serviceTypeInfo, transitions));
        }
        for (Map.Entry<Integer, Map<Integer, Double>> entry : transitionProbabilities.entrySet()) {
            int node = entry.getKey();
            NodeInfo nodeInfo = nodeInfos.get(node);
            Map<NodeInfo, Double> transitions = nodeInfo.getTransitionProbabilities();
            for (Map.Entry<Integer, Double> transition : entry.getValue().entrySet()) {
                transitions.put(nodeInfos.get(transition.getKey()), transition.getValue());
            }
        }
        return nodeInfos;
    }

    /*
      * @Description : 生成从所有入口节点到所有出口节点的所有路径及其概率。
      * @Author : Dior
      *  * @param adjMatrix
 * @param nodeInfos
      * @return : java.util.List<utils.PathProbability>
      * @Date : 2024/6/14 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static List<PathProbability> generateAllPaths(int[][] adjMatrix, Map<Integer, NodeInfo> nodeInfos) {
        List<PathProbability> pathProbabilities = new ArrayList<>();
        Set<Integer> entryNodes = new HashSet<>();
        Set<Integer> exitNodes = new HashSet<>();

        // 找到所有入口节点（入度为0）
        for (int i = 0; i < adjMatrix.length; i++) {
            boolean isEntry = true;
            for (int j = 0; j < adjMatrix.length; j++) {
                if (adjMatrix[j][i] == 1) {
                    isEntry = false;
                    break;
                }
            }
            if (isEntry) {
                entryNodes.add(i);
            }
        }

        // 找到所有出口节点（出度为0）
        for (int i = 0; i < adjMatrix.length; i++) {
            boolean isExit = true;
            for (int j = 0; j < adjMatrix.length; j++) {
                if (adjMatrix[i][j] == 1) {
                    isExit = false;
                    break;
                }
            }
            if (isExit) {
                exitNodes.add(i);
            }
        }

        // 生成所有路径及其概率
        for (int entryNode : entryNodes) {
            List<NodeInfo> currentPath = new ArrayList<>();
            currentPath.add(nodeInfos.get(entryNode)); // 存储 NodeInfo 而不是节点编号
            dfs(adjMatrix, nodeInfos, entryNode, currentPath, 1.0, pathProbabilities, exitNodes);
        }

        return pathProbabilities;
    }

    private static void dfs(int[][] adjMatrix, Map<Integer, NodeInfo> nodeInfos, int currentNode,
                            List<NodeInfo> currentPath, double currentProbability, List<PathProbability> pathProbabilities,
                            Set<Integer> exitNodes) {
        if (exitNodes.contains(currentNode)) {
            pathProbabilities.add(new PathProbability(new ArrayList<>(currentPath), currentProbability));
            return;
        }
        Map<NodeInfo, Double> transitions = nodeInfos.get(currentNode).getTransitionProbabilities();
        if (transitions != null) {
            for (Map.Entry<NodeInfo, Double> entry : transitions.entrySet()) {
                currentPath.add(entry.getKey()); // 存储 NodeInfo 而不是节点编号
                dfs(adjMatrix, nodeInfos, findNodeKey(nodeInfos, entry.getKey()), currentPath, currentProbability * entry.getValue(), pathProbabilities, exitNodes);
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    /*
      * @Description : 根据 NodeInfo 对象找到相应的节点键。
      * @Author : Dior
      *  * @param nodeInfos
 * @param nodeInfo
      * @return : java.lang.Integer
      * @Date : 2024/6/17
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static Integer findNodeKey(Map<Integer, NodeInfo> nodeInfos, NodeInfo nodeInfo) {
        for (Map.Entry<Integer, NodeInfo> entry : nodeInfos.entrySet()) {
            if (entry.getValue().equals(nodeInfo)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /*
      * @Description : 将 DAGPathInfo 对象序列化为 JSON 文件并保存。
      * @Author : Dior
      *  * @param dagPathInfo
 * @param fileName
      * @return : void
      * @Date : 2024/6/14 
      * @Version : 1.0
      * @Copyright : © 2024 All Rights Reserved.       
      **/
    private static void saveToJsonFile(AppPathInfo dagPathInfo, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(fileName), dagPathInfo);
    }
}
