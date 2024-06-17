package utils;

/**

 * 皮尔逊相关系数

 */

public class Pearson {

    public static void main(String[] args) {

        test();

    }

    private static void test(){

        /*用于测试*/

        double[] x = new double[] { 38 ,13 ,27, 25, 18, 29, 30, 20, 23, 32, 38, 28, 34, 19, 20, 25, 16, 36, 25, 17, 24, 18, 30, 35, 22, 34, 12, 26, 29, 21};

        //double[] y = new double[] { 49 ,42, 45, 39, 30, 25, 39, 36, 39 ,45, 42, 50, 36, 48, 35, 42, 45, 29, 33, 27, 42, 43, 27, 39, 37, 36, 47, 37, 44, 34};

        double[] y = new double[] {114, 49, 84, 79, 87, 74, 77, 82, 80, 88, 123, 82, 98, 65, 61, 78, 51, 121, 78, 50, 75, 65, 113, 122, 78, 119, 45, 89, 102, 75};

        double score = getPearsonCorrelationScore(x, y);

        System.out.println(score);//0.6350393282549671

    }

    public static double getPearsonCorrelationScore(double[] xData, double[] yData) {

        if (xData.length != yData.length)

            throw new RuntimeException("数据不正确！");

        double xMeans;

        double yMeans;

        double numerator = 0;// 求解皮尔逊的分子

        double denominator = 0;// 求解皮尔逊系数的分母

        double result = 0;

        // 拿到两个数据的平均值

        xMeans = getMeans(xData);

        yMeans = getMeans(yData);

        // 计算皮尔逊系数的分子

        numerator = generateNumerator(xData, xMeans, yData, yMeans);

        // 计算皮尔逊系数的分母

        denominator = generateDenomiator(xData, xMeans, yData, yMeans);

        // 计算皮尔逊系数

        result = numerator / denominator;

        return result;

    }

    /**

     * 计算分子

     *

     * @param xData

     * @param xMeans

     * @param yData

     * @param yMeans

     * @return

     */

    private static double generateNumerator(double[] xData, double xMeans, double[] yData, double yMeans) {

        double numerator = 0.0;

        for (int i = 0; i < xData.length; i++) {

            numerator += (xData[i] - xMeans) * (yData[i] - yMeans);

        }

        return numerator;

    }

    /**

     * 生成分母

     *

     * @param yMeans

     * @param yData

     * @param xMeans

     * @param xData

     * @return 分母

     */

    private static double generateDenomiator(double[] xData, double xMeans, double[] yData, double yMeans) {

        double xSum = 0.0;

        for (int i = 0; i < xData.length; i++) {

            xSum += (xData[i] - xMeans) * (xData[i] - xMeans);

        }

        double ySum = 0.0;

        for (int i = 0; i < yData.length; i++) {

            ySum += (yData[i] - yMeans) * (yData[i] - yMeans);

        }

        return Math.sqrt(xSum) * Math.sqrt(ySum);

    }

    /**

     * 根据给定的数据集进行平均值计算

     *

     * @param

     * @return 给定数据集的平均值

     */

    private static double getMeans(double[] datas) {

        double sum = 0.0;

        for (int i = 0; i < datas.length; i++) {

            sum += datas[i];

        }

        return sum / datas.length;

    }

}