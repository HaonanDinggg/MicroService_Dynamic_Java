package utils;

/**
 * 分数类
 */
public class Fraction {

    private int numerator;//分子
    private int denominator;//分母

    public Fraction(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }


    public double toDouble() {
        //利用JAVA中，整型与浮点型计算时会自动转化为浮点数的特点，进行格式转换
        return 1.0*numerator/denominator;
    }


    public Fraction multiply(Fraction r) {
        //定义乘法函数，返回类型为分数（Fraction)
        Fraction d = new Fraction(0,0);
        d.numerator = this.numerator * r.numerator;
        d.denominator = this.denominator * r.denominator;
        return d;
    }


    public void print() {
        //定义打印函数
        if (numerator==denominator) {
            System.out.println("1");
        }
        else {
            System.out.println(this.numerator + "/" + this.denominator);
        }
    }

    public Fraction simplify() {
        //简化函数
        for(int i=1;i<=numerator;i++) {
            if(numerator%i==0&&denominator%i==0) {
                numerator/=i;
                denominator/=i;
                i=1;
            }
        }
        Fraction d = new Fraction(numerator,denominator);
        return d ;
    }

    public static void main(String args[]){
        Fraction fraction = new Fraction(64,128);
        fraction.simplify();
        Fraction a = fraction.multiply(new Fraction(2,3));
        System.out.println(fraction.numerator);
        System.out.println(fraction.denominator);
        System.out.println(a.numerator);
        System.out.println(a.denominator);
    }

}
