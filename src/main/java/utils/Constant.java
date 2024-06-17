package utils;


import java.util.*;


public class Constant {
    //我们在这边确定请求流数量以及各自对应的请求流到达率时要考虑到我们在waittingtime中阶乘无法计算过大数据 因此我们的初始化数据不能选取过大
    //否则出现数据越界情况
    public static final int NO_OF_HOST = 50;//节点数量|V| 10-50
    public static final int RESOURCE_OF_HOST = 10;//每个服务器节点的核心数量
    public static final int NO_OF_MICROSERVICE = 10;//微服务种类数量|M| 2-40
    public static final int STREAM_NUM = 50; //请求流的数量 10-1000
    public static final double  THRESHOLD_OMEGA = 25;//w(部署算法中选择可用服务器)
    public static final double[] PROCESSING_RATE = new double[NO_OF_MICROSERVICE];//每种微服务的单位核心处理速率Λ_m
    public static final double[] MS_TOLERABLE_TIME = new double[NO_OF_MICROSERVICE];//每种微服务的最大可容忍时延
    public static final double[] STREAM_TOLERABLE_TIME = new double[STREAM_NUM];//每条流的最大可容忍时延
    public static final double[][] Trans_MS_USE = new double[NO_OF_MICROSERVICE][NO_OF_MICROSERVICE];  //在单位请求到达率下，从微服务h_i到微服务h_j传输数据消耗的带宽资源
    public static final double[][] Trans_HOST_Delay = new double[NO_OF_HOST][NO_OF_HOST]; //服务器之间的通信时延  即·节点间距离
    public static final double[][] HOST_Bandwith_Delay = new double[NO_OF_HOST][NO_OF_HOST]; //服务器之间的带宽
    public static final double[][] STREAM_DIST = new double[STREAM_NUM][STREAM_NUM]; //请求流之间的相似度矩阵
    public static  final ArrayList<ArrayList> All_Req_Stream = new ArrayList<>();//此数组存储拓扑排序的微服务以及进入节点以及最大容忍时延
    //因此我们需要用All_Req_Stream_Map来生产对应的All_Req_Stream
    public static  final ArrayList<ArrayList> K8s_Req_Stream = new ArrayList<>();//此数组存储拓扑排序的微服务以及进入节点以及最大容忍时延
    public static final ArrayList<Map<Integer, Integer>> All_Req_Stream_Map = new ArrayList<>();
    public static final  ArrayList<int[][]> All_Req_Stream_Matrix = Generate_All_Req_Stream_randomly();//此数组存储会被其他函数引用
    // 记录节点的入度
    public static int[] indegree;
    // 记录已经遍历的节点
    public static boolean[] visited;
    // 记录拓扑排序的结果
    public static List<Integer> result;
    //All_Req_Stream:全体用户请求流集合 M:请求流依次历经的微服务 v:请求流入口节点 lambda:请求流到达率
    //                 Req_Stream
    //Stream_1 [M_1=[0,2,4],  v1,lambda_1]
    //Stream_2 [M_2=[1,3,5,7],v2,lambda_2]
    //……
//    public static final ArrayList<ArrayList> All_Req_Stream = Generate_All_Req_Stream();


    public static ArrayList<ArrayList> Generate_All_Req_Stream(){
        ArrayList<ArrayList> all_Req_Stream = new ArrayList<>();
        int avg_arrival_rate = 2;
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(5,7,2,8,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(0,9,2,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(1,2,3,4,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(4,1,5,7,2,8,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(1,7,10,11,5,-1,avg_arrival_rate)));
//        all_Req_Stream.add(new ArrayList<>(Arrays.asList(1,7,2,11,5,-1,avg_arrival_rate))); //不是所有种类的微服务都被使用到
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(6,3,0,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(2,4,5,7,3,11,8,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(9,4,6,2,3,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(7,6,2,-1,avg_arrival_rate)));
        all_Req_Stream.add(new ArrayList<>(Arrays.asList(8,3,2,6,1,-1,avg_arrival_rate)));
        return all_Req_Stream;
    }

    //此处函数用于生成我们的用户请求 因此dag图也应该在此处生成
    //我们需要先随机生成边数和节点数(即我们的请求中微服务数量)，这两者之间也存在约束
    //节点数为n 则边数最少为n-1 最多为n(n-1)/2
    //我们需要用拓扑排序的方法来构建一个链表来进行nextfit的部署
    //但是在计算时延时需要考虑dag图带来的最小路径的计算问题
    //我们生成的拓扑排序里面可以存储我们的进入节点以及最大时延要求
    /* public static ArrayList<ArrayList> Generate_All_Req_Stream_randomly(){


     }
    */
    public static ArrayList<int[][]> Generate_All_Req_Stream_randomly(){
        Random random = new Random(214); //随机数，设置随机种子，默认为当前时间，也可以自己指定Random random = new Random(100)
        //生产对应矩阵 可以将生成过程放到方法中

        //生产一个list用于存储索引号与对应的微服务序号
        //需要将对应node的微服务进行随机生成 同时需要对dag进行拓扑排序获得一个部署的顺序
        //需要用dfs来遍历我们的图以便于图中多个访问序列部署的进行部署 将一个图多个服务处理链
        ArrayList<Map<Integer, Integer>> all_Req_Stream_Map = new ArrayList<>();
        ArrayList<ArrayList<Integer>> all_Req_Stream_topology = new ArrayList<>();
        ArrayList<int[][]> all_Req_Stream_matrix = new ArrayList<>();
        //因为不能仅靠拓扑排序还原出邻接矩阵，所以还需要一个arraylist来存储对应的邻接矩阵
        for (int i = 0; i < STREAM_NUM; i++) {
            //随机生成微服务的dag图M，节点的数量为2-8
            int node = random.nextInt(2)+4;
            //System.out.println(node);
            int edge = 0;
            double chainrate = 0;
            Random randomType = new Random();

            // 生成0到1之间的随机double数
            double randomValue = randomType.nextDouble();

            if(randomValue < chainrate){
                edge = node-1;//表示生成请求链的概率为chainrate 否则生成图数据
            }else {
                edge = random.nextInt((node * (node - 1) / 2) - node + 2) + node - 1;//边数存在限制
            }
            //System.out.println("图的节点数"+node+" 边数"+edge);
            int[][] DAG = CorrectGraph(node, edge);//此方法生产了一个保证唯一起点以及终点的DAG图
            // 打印生成的邻接矩阵
            //for (int[] row : DAG) {
            //System.out.println(Arrays.toString(row));
            //}
            all_Req_Stream_matrix.add(DAG);//用All_Req_Stream_Matrix来存储dag图的邻接矩阵
            List<Integer>topology = topologicalSort(DAG).subList(0, node);//此方法根据DAG生成拓扑排序
            //System.out.println(topology);
            Map<Integer, Integer> single_req_flow = new LinkedHashMap<>();
            //我们在其他模块中都用到了表示为arraylist的single stream参数，因此我们需要生成可被识别的函数
            //使用dfs遍历生产多个序列，每个序列可以看作一个以前的请求链
            //现在的问题是如何保存对应的图的进入节点以及请求到达率
            ArrayList<Integer> single_req_flow_topology = new ArrayList<>();
            ArrayList<Integer> k8s_single_req_flow_topology = new ArrayList<>();
            //问题在于我们请求流真的是拓扑排序吗？
            for(int j = 0;j < node  ;j++){
                int temp=random.nextInt(NO_OF_MICROSERVICE);//随机生产某一node上的某一微服务 将两者关联起来
                int NodeIndex = topology.get(j);//获取拓扑排序第j个节点的索引

                if(j==0){
                    single_req_flow.put(NodeIndex, temp);//添加对应DAG的节点索引以及对应的微服务编号
                    single_req_flow_topology.add(temp);//根据拓扑排序的顺序添加微服务序号 我们通过上面的single_req_flow获取对应的该索引编号的DAG图的节点索引
                    k8s_single_req_flow_topology.add(temp);
                    // single_req_flow.get(index)可以获取对应map
                    continue;
                }
                if (j!=0 && single_req_flow.containsValue(temp)){//保证一个服务请求中每种微服务只出现一次 重复则该循环重复一次
                    j--;
                }else {
                    single_req_flow.put(NodeIndex, temp);
                    single_req_flow_topology.add(temp);
                    k8s_single_req_flow_topology.add(temp);
                }
            }
            System.out.println("第"+(i+1)+"条请求流的微服务链");
            System.out.println(single_req_flow_topology);
            K8s_Req_Stream.add(k8s_single_req_flow_topology);
            // System.out.println();
            //ArrayList<Integer> single_req_flow = (ArrayList<Integer>) utils.randomUtil.randomNum(random.nextInt(3,6),0, NO_OF_MICROSERVICE);
            //入口节点(暂时用-1表示)
            single_req_flow_topology.add(-1);
            //随机生成到达率 1-10
            single_req_flow_topology.add(random.nextInt(1)+1);
            System.out.println("到达率为"+single_req_flow_topology.get(single_req_flow_topology.size()-1));
            //请求流可以相同
            //我们这边生成的single_req_flow_topology的索引是与single_req_flow一样的
            //通过索引可以关联起来
            All_Req_Stream_Map.add(single_req_flow);//按照拓扑排序存储dag图id与其对应节点的微服务
            All_Req_Stream.add(single_req_flow_topology);//这部操作是否合规 存储拓扑排序以及对应请求进入节点和到达率
//            //请求流不相同
//            if (!all_Req_Stream.contains(single_req_flow)){
//                all_Req_Stream.add(single_req_flow);
//            }
//            //若有重复的请求流则重新生成一条
//            else{
//                i--;
//            }
        }
        return all_Req_Stream_matrix;
    }

    // node节点数量，edge边数量
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
    // 判断边是否已经生成过
    private static boolean containsEdge(List<int[]> edges, int x, int y) {
        for (int[] edgeArr : edges) {
            if (edgeArr[0] == x && edgeArr[1] == y) {
                return true;
            }
        }
        return false;
    }
    //生成DAG图 一个入度为0的节点和一个出度为0的节点
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
                for(int j = 0 ; j < node ; j++) {
                    if(adjMatrix[i][j] == 1) {
                        rowjudge = false;//如果在某一行出现一个1则表示不为终点 该行一定不全0 就是对出度的判断
                    }
                }
                if(rowjudge) {
                    rowflag++;//出现一行全0则表示出现一个终点
                }
                rowjudge = true;//每次判断重置flag
            }
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


    //拓扑排序
    public static List<Integer> topologicalSort(int[][] adjacencyMatrix) {
        int n = adjacencyMatrix.length;
        indegree = new int[n];
        visited = new boolean[n];
        result = new ArrayList<>();

        // 统计每个节点的入度
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[i][j] == 1) {
                    indegree[j]++;
                }
            }
        }

        // 从入度为0的节点开始遍历
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                dfs(i,adjacencyMatrix);
            }
        }

        return result;
    }

    //dfs遍历
    private static void dfs(int i,int[][] adjacencyMatrix) {
        visited[i] = true;
        result.add(i);
        for (int j = 0; j < adjacencyMatrix.length; j++) {
            if (adjacencyMatrix[i][j] == 1 && !visited[j]) {
                indegree[j]--;
                if (indegree[j] == 0) {
                    dfs(j,adjacencyMatrix);
                }
            }
        }
    }

    public static void main(String[] args) {
    }
}
