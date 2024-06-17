package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//public class randomUtil {
//    public static List<Integer> randomNum(int scope, int origin, int bound) {
//        List<Integer> mylist = new ArrayList<>(); // 用于储存不重复的随机数
//        Random rd = new Random();
//        while (mylist.size() < scope) {
//            int myNum = rd.nextInt(0, bound);
//            if (!mylist.contains(myNum)) { // 判断容器中是否包含指定的数字
//                mylist.add(myNum); // 往集合里面添加数据。
//            }
//        }
//        return mylist;
//    }
//}

public class randomUtil {
    public static List<Integer> randomNum(int scope, int origin, int bound) {
        List<Integer> mylist = new ArrayList<>(); // 用于储存不重复的随机数
        Random random = new Random();
        while (mylist.size() < scope) {
            int myNum = random.nextInt(bound);
            if (!mylist.contains(myNum)) { // 判断容器中是否包含指定的数字
                mylist.add(myNum); // 往集合里面添加数据。
            }
        }
        return mylist;
    }
}
