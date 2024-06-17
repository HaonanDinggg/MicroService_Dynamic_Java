package utils;

import java.util.*;
import java.util.function.DoubleToLongFunction;
import sun.security.util.Length;

import static utils.Constant.*;


public class WaitingTime {
    public static int[] Fail = new int[STREAM_NUM];
    public static int[] G_Fail = new int[STREAM_NUM];
    public static int[] FFD_Fail = new int[STREAM_NUM];
    public static int[] A_Fail = new int[STREAM_NUM];

    public static int[] R_Fail = new int[STREAM_NUM];





    //微服务链(请求流)的最大时延
    public static double[] Stream_Max_Time = new double[STREAM_NUM];
    //微服务链的平均时延
    public static double[] Stream_Average_Time = new double[STREAM_NUM];
    //部署微服务m镜像实例的节点的请求平均逗留时延
    public static double[] MS_Average_Time = new double[NO_OF_MICROSERVICE];

    //每条服务链上每种微服务在每个服务器上的到达率比例
    public static double[][][] Stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的到达率
    public static double[][] MS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的时延
    public static double[][] MS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];

    //对比算法
    //微服务链的最大时延
    public static double[] G_Stream_Max_Time = new double[NO_OF_MICROSERVICE];
    //微服务链的平均时延
    public static double[] G_Stream_Average_Time = new double[NO_OF_MICROSERVICE];
    //部署微服务m镜像实例的节点的请求平均逗留时延
    public static double[] G_MS_Average_Time = new double[NO_OF_MICROSERVICE];
    //每条服务链上每种微服务在每个服务器上的到达率比例
    public static double[][][] G_Stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的到达率
    public static double[][] G_MS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //种微服务在每个服务器上的时延
    public static double[][] G_MS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    public static int G_Full_Use_Host = 0;


    //对比算法FFD
    //微服务链的最大时延
    public static double[] FFD_Stream_Max_Time = new double[NO_OF_MICROSERVICE];
    //微服务链的平均时延
    public static double[] FFD_Stream_Average_Time = new double[NO_OF_MICROSERVICE];
    //部署微服务m镜像实例的节点的请求平均逗留时延
    public static double[] FFD_MS_Average_Time = new double[NO_OF_MICROSERVICE];
    //每条服务链上每种微服务在每个服务器上的到达率比例
    public static double[][][] FFD_Stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的到达率
    public static double[][] FFD_MS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //种微服务在每个服务器上的时延
    public static double[][] FFD_MS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    public static int FFD_Full_Use_Host = 0;

    //对比算法Assign
    //微服务链的最大时延
    public static double[] A_Stream_Max_Time = new double[NO_OF_MICROSERVICE];
    //微服务链的平均时延
    public static double[] A_Stream_Average_Time = new double[NO_OF_MICROSERVICE];
    //部署微服务m镜像实例的节点的请求平均逗留时延
    public static double[] A_MS_Average_Time = new double[NO_OF_MICROSERVICE];
    //每条服务链上每种微服务在每个服务器上的到达率比例
    public static double[][][] A_Stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的到达率
    public static double[][] A_MS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //种微服务在每个服务器上的时延
    public static double[][] A_MS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    public static int A_Full_Use_Host = 0;

    //对比算法random
    //微服务链的最大时延
    public static double[] R_Stream_Max_Time = new double[NO_OF_MICROSERVICE];
    //微服务链的平均时延
    public static double[] R_Stream_Average_Time = new double[NO_OF_MICROSERVICE];
    //部署微服务m镜像实例的节点的请求平均逗留时延
    public static double[] R_MS_Average_Time = new double[NO_OF_MICROSERVICE];
    //每条服务链上每种微服务在每个服务器上的到达率比例
    public static double[][][] R_Stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //每种微服务在每个服务器上的到达率
    public static double[][] R_MS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    //种微服务在每个服务器上的时延
    public static double[][] R_MS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
    public static int R_Full_Use_Host = 0;

    public static int F_Activated_Host_Num = 0;
    public static int F_Activated_MS_Num = 0;
    public static double F_Resource_Utilization_Rate = 0;

    public static int A_Activated_Host_Num = 0;
    public static int A_Activated_MS_Num = 0;
    public static double A_Resource_Utilization_Rate = 0;

    public static int G_Activated_Host_Num = 0;
    public static int G_Activated_MS_Num = 0;
    public static double G_Resource_Utilization_Rate = 0;

    public static int R_Activated_Host_Num = 0;
    public static int R_Activated_MS_Num = 0;
    public static double R_Resource_Utilization_Rate = 0;

    public static int N_Activated_Host_Num = 0;
    public static int N_Activated_MS_Num = 0;
    public static double N_Resource_Utilization_Rate = 0;
    public static int N_Full_Use_Host = 0;

    public static int MCRA_Activated_Host_Num = 0;
    public static int MCRA_Activated_MS_Num = 0;
    public static double MCRA_Resource_Utilization_Rate = 0;
    public static int MCRA_Full_Use_Host = 0;

    public static int Final_Activated_Host_Num = 0;
    public static int Final_Activated_MS_Num = 0;
    public static double Final_Resource_Utilization_Rate = 0;
    public static int Final_Full_Use_Host = 0;
    /**
     *
     * @param stream_allocated_resource
     * @return 对于每条服务请求流，计算包含的每种微服务在实例化镜像的处理器上的到达率比例
     */
    public static double[][][] get_Stream_Arrival_Rate(int[][][] stream_allocated_resource) {
        int[][][] stream_Allocated_Resource = stream_allocated_resource;
        double[][][] stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
        for (int i = 0; i < All_Req_Stream.size(); i++) {
            for (int j = 0; j < Constant.NO_OF_MICROSERVICE; j++) {
                if (Arrays.stream(stream_Allocated_Resource[i][j]).sum() != 0) {
                    for (int k = 0; k < Constant.NO_OF_HOST; k++) {
                        stream_Arrival_Rate[i][j][k] = stream_Allocated_Resource[i][j][k] * 1.00 / Arrays.stream(stream_Allocated_Resource[i][j]).sum();
                    }
                }
            }
        }
        return stream_Arrival_Rate;
    }

    /**
     *
     * @param stream_allocated_resource
     * @return 对于每条服务请求流，计算包含的每种微服务在实例化镜像的处理器上的到达率比例
     */
    public static double[][][] get_Stream_Arrival_Rate_T(int[][][] stream_allocated_resource) {
        int[][][] stream_Allocated_Resource = stream_allocated_resource;
        double[][][] stream_Arrival_Rate = new double[Constant.All_Req_Stream.size()][Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
        for (int i = 0; i < All_Req_Stream.size(); i++) {
            ArrayList<Integer> single_Req_Stream = All_Req_Stream.get(i);
            int arrival_rate = single_Req_Stream.get(single_Req_Stream.size() - 1);
            for (int j = 0; j < Constant.NO_OF_MICROSERVICE; j++) {
                if (Arrays.stream(stream_Allocated_Resource[i][j]).sum() != 0) {
                    for (int k = 0; k < Constant.NO_OF_HOST; k++) {
                        System.out.println("请求流"+i+"的微服务"+j+"的总到达率");
                        stream_Arrival_Rate[i][j][k] = stream_Allocated_Resource[i][j][k] * 1.00 / Arrays.stream(stream_Allocated_Resource[i][j]).sum()*arrival_rate;
                    }
                }
            }
        }
        return stream_Arrival_Rate;
    }

    /**
     *
     * @param stream_arrival_rate
     * @return 计算每种微服务在每个在每个服务器上的到达率之和（求和所有请求流）
     */
    public static double[][] get_MS_Host_Arrival_Rate(double[][][] stream_arrival_rate) {
        double[][][] stream_Arrival_Rate = stream_arrival_rate;
        double[][] mS_Host_Arrival_Rate = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];
        for (int i = 0; i < All_Req_Stream.size(); i++) {
            ArrayList<Integer> single_Req_Stream = All_Req_Stream.get(i);
            int arrival_rate = single_Req_Stream.get(single_Req_Stream.size() - 1);
            for (int j = 0; j < Constant.NO_OF_MICROSERVICE; j++) {
                for (int k = 0; k < Constant.NO_OF_HOST; k++) {
                    mS_Host_Arrival_Rate[j][k] += stream_Arrival_Rate[i][j][k] * arrival_rate;
                }
            }
        }
        return mS_Host_Arrival_Rate;
    }

    /**
     *每种微服务在每个处理器上的总排队时延
     * @param mS_ocp_resource
     * @param mS_host_arrival_rate
     * @return 排队论平均响应时延计算，返回的是二维矩阵 微服务种类*处理器数量
     */
    public static double[][] get_MS_Stay_Time(int[][] mS_ocp_resource,double[][]mS_host_arrival_rate) {
        int[][] mS_Ocp_Resource=mS_ocp_resource;
        double[][] mS_Host_Arrival_Rate = mS_host_arrival_rate;
        //要返回的结果数组
        double[][] mS_Stay_Time = new double[Constant.NO_OF_MICROSERVICE][Constant.NO_OF_HOST];

        for (int i = 0; i < NO_OF_MICROSERVICE; i++) {
            for (int j = 0; j < NO_OF_HOST; j++) {
                if (mS_Ocp_Resource[i][j] > 0 && mS_Host_Arrival_Rate[i][j] > 0) {
//                    System.out.println(mS_Host_Arrival_Rate[i][j]);
//                    System.out.println(PROCESSING_RATE[i]);
//                    System.out.println(mS_Ocp_Resource[i][j]);
                    double service_rate1 = mS_Host_Arrival_Rate[i][j] / PROCESSING_RATE[i];
//                    System.out.println(service_rate1);
                    double service_rate2 = mS_Host_Arrival_Rate[i][j] / PROCESSING_RATE[i] / mS_Ocp_Resource[i][j];
                    //如果满足服务强度小于1，则可以按照排队论计算公式计算
                    if (service_rate2 > 0 && service_rate2 < 1) {
//                    System.out.println(service_rate2);
                        double v1 = 0;
                        for (int k = 0; k < mS_Ocp_Resource[i][j]; k++) {
                            double v2 = JieCheng(k);
                            double v3 = Math.pow(service_rate1, k) * 1.000000 / v2;
                            v1 += v3;
                        }
//                    System.out.println(v1);
                        int v4 = mS_Ocp_Resource[i][j];
                        double v5 = JieCheng(v4);

//                    System.out.println(v5);
                        double p0 = Math.pow((v1 + mS_Ocp_Resource[i][j] * 1.000000 * Math.pow(service_rate1, mS_Ocp_Resource[i][j]) / (v5 * (mS_Ocp_Resource[i][j] - service_rate1))), -1);
//                    System.out.println(p0);
//                 mS_Stay_Time[i][j] = service_rate2 * Math.pow((service_rate2 * mS_Ocp_Resource[i][j]), mS_Ocp_Resource[i][j]) * p0 / (mS_Host_Arrival_Rate[i][j] * Math.pow((1 - service_rate2), 2) * v5) + 1.000000 / PROCESSING_RATE[i];
                    mS_Stay_Time[i][j] = service_rate2 * Math.pow(service_rate2, mS_Ocp_Resource[i][j]) * p0 / (mS_Host_Arrival_Rate[i][j] * Math.pow((1 - service_rate2), 2) * v5) + 1.000000 / PROCESSING_RATE[i];
//                    System.out.println(mS_Stay_Time[i][j]);
//                    System.out.println("********" + "\t");
                    }else{//服务强度大于1，则需要减成一队，按照先到先服务的要求来计算时延，此处默认为整个流的最大容忍时延
                        mS_Stay_Time[i][j]=Arrays.stream(Constant.STREAM_TOLERABLE_TIME).max().getAsDouble();
                    }
                }
            }
        }
        return mS_Stay_Time;
    }

    public static double get_MS_Time(double Arrival_Rate,double PROCESSING_RATE,int REQ_Resource) {
        double service_rate1 = Arrival_Rate / PROCESSING_RATE;
//        System.out.println("service_rate1" + service_rate1);

        double service_rate2 = Arrival_Rate / PROCESSING_RATE / REQ_Resource;
//        System.out.println("service_rate2" + service_rate2);

        double v1 = 0;
//        System.out.println("jiecheng" );
        for (int k = 0; k < REQ_Resource; k++) {
            double v2 = JieCheng(k);
//            System.out.println("v2  "+ v2 );
            double v3 = Math.pow(service_rate1, k) / v2;
//            System.out.println("v3  "+ v3);
            v1 += v3;
        }
//        System.out.println("v1" + v1);

        double v5 = JieCheng(REQ_Resource);
//        System.out.println("v5" + v5);

        double p0 = Math.pow((v1 +  Math.pow(service_rate1, REQ_Resource) / (v5 * (1 - service_rate2))), -1);
//        System.out.println("p0" + p0);

        double MS_Time = service_rate2 * Math.pow(service_rate1, REQ_Resource) * p0 / (Arrival_Rate * Math.pow((1 - service_rate2), 2) * v5) + 1.000000 / PROCESSING_RATE;
//        System.out.println("MS_Time" + MS_Time);

        return MS_Time;
    }

    /**
     *
     * @param stream_allocated_resource
     * @param mS_stay_time
     * @return 计算请求流的最大时延，返回一维数组，大小为流数量大小
     */
    public static double[] get_Stream_Max_Time(int[][][] stream_allocated_resource, double[][] mS_stay_time) {
        int[][][] stream_Allocated_Resource=stream_allocated_resource;
        double[][] mS_Stay_Time=mS_stay_time;
        //时延存储的结果
        double[] Stream_Max_Time = new double[STREAM_NUM];
        for (int i = 0; i < All_Req_Stream.size(); i++) {//每个请求流
            ArrayList<Integer> single_Req_Stream = All_Req_Stream.get(i);
            double stream_time=0; //记录累加最长时间
            int count=0; //记录次数
            int arrival_rate = single_Req_Stream.get(single_Req_Stream.size() - 1);//请求流的到达率

            for (int j = 0; j < Constant.NO_OF_MICROSERVICE; j++) {
                if (Arrays.stream(stream_Allocated_Resource[i][j]).sum() != 0) {//挑选包含的微服务
                    double max = 0;
                    count+=1;
                    for (int k = 0; k < Constant.NO_OF_HOST; k++) {
                        if (stream_Allocated_Resource[i][j][k] >0) {
//                            if (mS_Stay_Time[j][k]*stream_Arrival_Rate[i][j][k]*arrival_rate/mS_Host_Arrival_Rate[j][k] > max) {
//                                max = mS_Stay_Time[j][k]*stream_Arrival_Rate[i][j][k]*arrival_rate/mS_Host_Arrival_Rate[j][k];
//                            }
                            if (mS_Stay_Time[j][k] > max) {
                                max = mS_Stay_Time[j][k];
                            }
                        }
                    }
                    stream_time += max;
                }
            }

            if (count == single_Req_Stream.size()-2){
                Stream_Max_Time[i]=stream_time;
            }else{
                //表示服务失败,时延为无穷大
                Stream_Max_Time[i]=Double.MAX_VALUE;
            }
        }
        return Stream_Max_Time;
    }

    /**
     *
     * @param stream_allocated_resource
     * @param stream_arrival_rate
     * @param mS_stay_time
     * @return 计算请求流的平均时延，概率路径期望，返回一维数组，大小为流数量大小
     */
    public static double[] get_Stream_Average_Time(int[][][] stream_allocated_resource, double[][][] stream_arrival_rate,
                                               double[][] mS_stay_time) {
        int[][][] stream_Allocated_Resource=stream_allocated_resource;
        double[][][] stream_Arrival_Rate=stream_arrival_rate;
        double[][] mS_Stay_Time=mS_stay_time;
        //存储结果
        double[] stream_Average_Time = new double[STREAM_NUM];
        for (int i = 0; i < All_Req_Stream.size(); i++) {
            ArrayList<Integer> single_Req_Stream = All_Req_Stream.get(i);
            int arrival_rate = single_Req_Stream.get(single_Req_Stream.size() - 1);
            double ms_average_time=0;
            double ms_node=0;
            for (int j = 0; j < Constant.NO_OF_MICROSERVICE; j++) {
//                double ms_node=0;
                if (Arrays.stream(stream_Allocated_Resource[i][j]).sum()>0){
                    ms_node+=1;
                    for (int k = 0; k < Constant.NO_OF_HOST; k++) {
                        if (stream_Allocated_Resource[i][j][k] >0) {
//                        ms_average_time +=mS_Stay_Time[j][k]*stream_Arrival_Rate[i][j][k]*arrival_rate/mS_Host_Arrival_Rate[j][k];
                            ms_average_time +=mS_Stay_Time[j][k]*stream_Arrival_Rate[i][j][k];
//                        ms_node++;
                        }
                    }
                }
//                if(ms_node!=0){
//                    ms_average_time=ms_average_time/ms_node;
//                    stream_Average_Time[i]+= ms_average_time;
//                }
            }
            if (ms_node==single_Req_Stream.size()-2){
                stream_Average_Time[i]=ms_average_time;
            }else{
                stream_Average_Time[i]=Double.MAX_VALUE;
            }
        }
        return stream_Average_Time;
    }

    /**
     *
     * @param mS_stay_time
     * @param mS_ocp_resource
     * @return 计算微服务的在所有处理器上（部署有镜像）的平均时延，返回一维数组，大小为微服务数量大小，见论文公式定义
     */
    public static double[] get_MS_Average_Time(double[][] mS_stay_time,int[][] mS_ocp_resource) {
        double[][] mS_Stay_Time=mS_stay_time;
        int[][] mS_Ocp_Resource=mS_ocp_resource;

        double[] mS_Average_Time = new double[NO_OF_MICROSERVICE];
        for (int i = 0; i < Constant.NO_OF_MICROSERVICE; i++) {
            double m_time = 0;
            double m_node = 0;
            for (int j = 0; j < Constant.NO_OF_HOST; j++) {
                if(mS_Ocp_Resource[i][j]>0 && mS_Stay_Time[i][j]>0) {
                    m_time += mS_Stay_Time[i][j];
                    m_node += mS_Ocp_Resource[i][j];
                }
            }
            mS_Average_Time[i] = m_time / m_node;
        }
        return mS_Average_Time;
    }


    /**
     *
     * @param stream_max_time
     * @param fail
     * @return 获取请求流服务成功率
     */
    public static double get_Success_Rate (double[] stream_max_time,int[] fail){
        double[] stream_Max_Time=stream_max_time;
        int[] Fail=fail;
        double num_fail=0;
        for (int i = 0; i < All_Req_Stream.size(); i++) {
            if(stream_Max_Time[i]>STREAM_TOLERABLE_TIME[i]){
                Fail[i]=1;
            }
        }

        for (int i = 0; i < All_Req_Stream.size(); i++) {
//            System.out.println("Stream"+i+"="+Fail[i]);
            if(Fail[i]==1){
                num_fail++;
            }
        }
        double success_Rate=1-num_fail/All_Req_Stream.size();
        return success_Rate;
    }


    //获取所有使用节点的平均逗留时延，应该只算部署成功的,未使用的不考虑

    /**
     *
     * @param mS_average_time
     * @param success_rate
     * @return 部署成功的所有微服务的单位核心平均处理能力
     */
    public static double get_Average_MS_Time(double[] mS_average_time,double success_rate) {
        double[] mS_Average_Time=mS_average_time;
        double sum_time=0;
        int count=0;
        for (int i = 0; i < Constant.NO_OF_MICROSERVICE; i++) {

            if (mS_Average_Time[i]>0){
                count+=1;
                sum_time += mS_Average_Time[i];
            }
        }
        if (success_rate!=0){
        double Average_MS_Time=sum_time/count/success_rate;
        return Average_MS_Time;
        }else{
            return Double.MAX_VALUE;
        }
    }

    //获取所有微服务流平均时延；只算服务成功的

    /**
     *
     * @param stream_average_time
     * @param success_rate
     * @return 所有请求的平均时延（整个系统）
     */
    public static double get_Average_Stream_Time(double[] stream_average_time,double success_rate) {
        double[] stream_Average_Time=stream_average_time;
        double sum_stream_time=0;
        int count=0;
        for (int i = 0; i < Constant.STREAM_NUM; i++) {

            if (stream_Average_Time[i]!=Double.MAX_VALUE){
                sum_stream_time += stream_Average_Time[i];
                count+=1;
            }
        }
        if (success_rate!=0){
            double Average_Stream_Time=sum_stream_time/count/success_rate;
            return Average_Stream_Time;
        }else{
            return Double.MAX_VALUE;
        }

    }


    
    /**
    *
     * @param Arrival_Rate
     * @param Req_Resource
     * @param Processing_Rate
    * @return 计算对应到达率 占用的资源数量 处理率的微服务的处理时延
    */
    
    
    
public static double Calculate_Stay_Time(double Arrival_Rate,int Req_Resource,double Processing_Rate) {
    	
	//        System.out.println(Arrival_Rate);
	//        System.out.println(Processing_Rate);
	//        System.out.println(Req_Resource);
	double Stay_Time = 0;
	double service_rate1 = Arrival_Rate / Processing_Rate;
	//        System.out.println(service_rate1);
	double service_rate2 = Arrival_Rate / Processing_Rate / Req_Resource;
	        //如果满足服务强度小于1，则可以按照排队论计算公式计算
	if (service_rate2 > 0 && service_rate2 < 1) {
	//        System.out.println(service_rate2);
		double v1 = 0;
		for (int k = 0; k < Req_Resource; k++) {
            double v2 = JieCheng(k);
				double v3 = Math.pow(service_rate1, k) * 1.000000 / v2;
				v1 += v3;
		}
	//        System.out.println(v1);
		int v4 = Req_Resource;
        double v5 = JieCheng(v4);//备注下 因为v4的阶乘太大了 无法存储 所以v5为0
	//        System.out.println(v5);
		double p0 = Math.pow((v1 + Req_Resource * 1.000000 * Math.pow(service_rate1, Req_Resource) / (v5 * (Req_Resource - service_rate1))), -1);
	//        System.out.println(p0);
	//     mS_Stay_Time[i][j] = service_rate2 * Math.pow((service_rate2 * mS_Ocp_Resource[i][j]), mS_Ocp_Resource[i][j]) * p0 / (mS_Host_Arrival_Rate[i][j] * Math.pow((1 - service_rate2), 2) * v5) + 1.000000 / PROCESSING_RATE[i];
		Stay_Time = service_rate2 * Math.pow(service_rate2, Req_Resource) * p0 / (Arrival_Rate * Math.pow((1 - service_rate2), 2) * v5) + 1.000000 / Processing_Rate;
	//        System.out.println(mS_Stay_Time[i][j]);
	//        System.out.println("********" + "\t");
		}else{//服务强度大于1，则需要减成一队，按照先到先服务的要求来计算时延，此处默认为整个流的最大容忍时延
		Stay_Time = Arrays.stream(Constant.STREAM_TOLERABLE_TIME).max().getAsDouble();
		}
		return Stay_Time;
}



    //阶乘

    /**
     *
     * @param num
     * @return 计算阶乘
     */
    private static double JieCheng(int num) {
        double sum = 1;
        if (num < 0) {
            throw new IllegalArgumentException("需要计算的参数必须为正数！");//抛出不合理参数异常
        }
        if (num == 0) {
            return 1;//跳出循环
        }
        if (num == 1) {
            return 1;//跳出循环
        } else {
            sum = num * JieCheng(num - 1);//递归
            return sum;
        }
    }



    //计算微服务满足延迟条件下所需的核心数
    //到达率可能需要强转类型
    //tolerable平划_Time应该为公分的时延
    public static int MS_Resource(double arrival_Rate , double processing_Rate, int resource, double tolerable_Time) {
        double Arrival_Rate = arrival_Rate;
        double Processing_Rate = processing_Rate;
        int Resource=  resource;
        double Tolerable_Time = tolerable_Time;
        double service_rate1 = Arrival_Rate / Processing_Rate;
        //System.out.println(service_rate1);
        int ms_Resource=0;
        for (int i = 0; i < Resource; i++) {
            int S=i+1;
            double service_rate2 = Arrival_Rate / Processing_Rate / S;
            // System.out.println(service_rate2);
            //排队论计算公式
            if(service_rate2<1&&service_rate2>0){
                double v1 = 0;
                for (int k = 0; k < S; k++) {
                    double v2 = JieCheng(k);
                    double v3 = Math.pow(service_rate1, k) * 1.000000 / v2;
                    v1 += v3;
                }
//                System.out.println(v1);
                double v4 = JieCheng(S);
                double p0 = Math.pow((v1 + 1.000000 * Math.pow(service_rate1, S) / (v4 * (1 - service_rate2))), -1);
//                System.out.println(p0);
                double ms_time = service_rate2 * Math.pow((service_rate2)*S, S) * p0 / (Arrival_Rate * Math.pow((1 - service_rate2), 2) * v4) + 1.00000 / Processing_Rate;
//                System.out.println(ms_time);
                if(ms_time<=Tolerable_Time) {//看你出现一直大于的情况
                    ms_Resource = S;
//                    System.out.println(ms_Resource);
                    break;
                }
            }
        }
        return ms_Resource;
    }




    /**
     *
     * @param single_Req_Stream 请求流信息
     * @param index_of_stream 请求流的编号
     * @param index_of_ms 要计算的微服务的编号
     * @param PROCESSING_RATE 单位核心对每种微服务的处理能力
     * @param STREAM_TOLERABLE_TIME 流期望的容忍时延
     * @return 计算请求中每种微服务的均分容忍时延大小
     */
    public static int calculate_MS_Resource(ArrayList<Integer> single_Req_Stream,int index_of_stream,int index_of_ms,double[] PROCESSING_RATE,double[] STREAM_TOLERABLE_TIME){
        ArrayList<Integer> Stream_Top = All_Req_Stream.get(index_of_stream);
        double Arrival_Rate = Stream_Top.get(Stream_Top.size()-1);
        double Processing_Rate = PROCESSING_RATE[index_of_ms];
        int Resource = Constant.RESOURCE_OF_HOST;
        //计算最大容忍时延
        double Tolerable_Time = calculate_MS_tolerable_Time_fairly(single_Req_Stream,index_of_stream,index_of_ms,PROCESSING_RATE,STREAM_TOLERABLE_TIME);
        double service_rate1 = Arrival_Rate / Processing_Rate;
        int ms_Resource=0;
        for (int i = 0; i < Resource; i++) {
            int S=i+1;
            double service_rate2 = Arrival_Rate / Processing_Rate / S;
            //排队论时延计算公式
            if(service_rate2<1 && service_rate2>0){
                double v1 = 0;//计算p0中的累加和部分
                for (int k = 0; k < S; k++) {
                    double v2 = JieCheng(k);
                    double v3 = Math.pow(service_rate1, k) * 1.000000 / v2;
                    v1 += v3;
                }
//                System.out.println(v1);
                double v4 = JieCheng(S);
                double p0 = Math.pow((v1 + 1.000000 * Math.pow(service_rate1, S) / (v4 * (1 - service_rate2))), -1);
//                System.out.println(p0);
                double ms_time = service_rate2 * Math.pow(service_rate2, S) * p0 / (Arrival_Rate * Math.pow((1 - service_rate2), 2) * v4) + 1.00000 / Processing_Rate;
//                System.out.println(ms_time);
                if(ms_time<=Tolerable_Time) {
                    ms_Resource = S;
                    break;
                }
            }
        }
        return ms_Resource;
    }

    public static double calculate_MS_tolerable_Time_fairly(ArrayList<Integer> single_Req_Stream,int index_of_stream,int index_of_ms,double[] PROCESSING_RATE,double[] STREAM_TOLERABLE_TIME){
        double reciprocal_sum=0;
        for(int i=0;i<single_Req_Stream.size()-2;i++){
            reciprocal_sum+=1.00/PROCESSING_RATE[single_Req_Stream.get(i)];
        }
        return (1.00/PROCESSING_RATE[index_of_ms]/reciprocal_sum)*STREAM_TOLERABLE_TIME[index_of_stream];
    }

    /**
     *
     * @param single_Req_Stream 请求流信息
     * @param index_of_stream 请求流的编号
     * @param index_of_ms 要计算的微服务的编号
     * @param PROCESSING_RATE 单位核心对每种微服务的处理能力
     * @param STREAM_TOLERABLE_TIME 流期望的容忍时延
     * @return 计算请求中每种微服务的均分容忍时延大小
     */
    public static int calculate_MS_Resource_2(ArrayList<Integer> single_Req_Stream,int index_of_stream,int index_of_ms,double[] PROCESSING_RATE,double[] STREAM_TOLERABLE_TIME){
        ArrayList<Integer> Stream_Top = All_Req_Stream.get(index_of_stream);
        double Arrival_Rate = Stream_Top.get(Stream_Top.size()-1);
        double Processing_Rate = PROCESSING_RATE[index_of_ms];
        int Resource = Constant.RESOURCE_OF_HOST;
        //计算最大容忍时延
        double Tolerable_Time = calculate_MS_tolerable_Time_fairly_2(single_Req_Stream,index_of_stream,index_of_ms,PROCESSING_RATE,STREAM_TOLERABLE_TIME);
        double service_rate1 = Arrival_Rate / Processing_Rate;
        int ms_Resource=0;
        for (int i = 0; i < Resource; i++) {
            int S=i+1;
            double service_rate2 = Arrival_Rate / Processing_Rate / S;
            //排队论时延计算公式
            if(service_rate2<1 && service_rate2>0){
                double v1 = 0;//计算p0中的累加和部分
                for (int k = 0; k < S; k++) {
                    double v2 = JieCheng(k);
                    double v3 = Math.pow(service_rate1, k) * 1.000000 / v2;
                    v1 += v3;
                }
//                System.out.println(v1);
                double v4 = JieCheng(S);
                double p0 = Math.pow((v1 + 1.000000 * Math.pow(service_rate1, S) / (v4 * (1 - service_rate2))), -1);
//                System.out.println(p0);
                double ms_time = service_rate2 * Math.pow(service_rate2, S) * p0 / (Arrival_Rate * Math.pow((1 - service_rate2), 2) * v4) + 1.00000 / Processing_Rate;
//                System.out.println(ms_time);
                if(ms_time<=Tolerable_Time) {
                    ms_Resource = S;
                    break;
                }
            }
        }
        return ms_Resource;
    }

    public static double calculate_MS_tolerable_Time_fairly_2(ArrayList<Integer> single_Req_Stream,int index_of_stream,int index_of_ms,double[] PROCESSING_RATE,double[] STREAM_TOLERABLE_TIME){
        double reciprocal_sum=0;
        for(int i=0;i<single_Req_Stream.size()-2;i++){
            reciprocal_sum+=1.00/(single_Req_Stream.size()-2);
        }
        return (1.00/PROCESSING_RATE[index_of_ms]/reciprocal_sum)*STREAM_TOLERABLE_TIME[index_of_stream];
    }




    public static ArrayList<Integer> calculate_activated_host(int[][] MS_Ocp_Resource){
        ArrayList<Integer> activated_host = new ArrayList<Integer>();;
        for(int i = 0 ; i< NO_OF_HOST; i++){
            for(int j = 0 ; j< NO_OF_MICROSERVICE; j++) {
                if(MS_Ocp_Resource[j][i] > 0){
                    activated_host.add(i);
                    break;
                }
            }
        }
        return activated_host;
    }

    public static int calculate_activated_ms(int[][] MS_Ocp_Resource){
        int activated_ms_num = 0;;
        for(int i = 0 ; i< NO_OF_MICROSERVICE; i++){
            for(int j = 0 ; j< NO_OF_HOST; j++) {
                activated_ms_num += MS_Ocp_Resource[i][j];
            }
        }
        return activated_ms_num;
    }

    public static void calculate_full_use_host(int[][] MS_Ocp_Resource){
        for(int i = 0 ; i< NO_OF_HOST; i++){
            int Ms_Used = 0;
            double Use_Rate = 0;
            for(int j = 0 ; j < NO_OF_MICROSERVICE ; j++) {
                Ms_Used+= MS_Ocp_Resource[j][i];
            }
            Use_Rate = (double)Ms_Used/RESOURCE_OF_HOST;
            if(Use_Rate>0){
                System.out.println(Use_Rate);
            }
        }
    }

    public static double calculate_resource_utilization_rate(int activated_ms_num,int activated_host_num){
        double rate = 0;;
        rate = (double) activated_ms_num/activated_host_num/RESOURCE_OF_HOST;
        return rate;
    }
}

