package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapSortUtil {
    private static Comparator<Map.Entry> comparatorByKeyAsc = (Map.Entry o1, Map.Entry o2) -> {
        if (o1.getKey() instanceof Comparable) {
            return ((Comparable) o1.getKey()).compareTo(o2.getKey());
        }
        throw new UnsupportedOperationException("键的类型尚未实现Comparable接口");
    };


    private static Comparator<Map.Entry> comparatorByKeyDesc = (Map.Entry o1, Map.Entry o2) -> {
        if (o1.getKey() instanceof Comparable) {
            return ((Comparable) o2.getKey()).compareTo(o1.getKey());
        }
        throw new UnsupportedOperationException("键的类型尚未实现Comparable接口");
    };


    private static Comparator<Map.Entry> comparatorByValueAsc = (Map.Entry o1, Map.Entry o2) -> {
        if (o1.getValue() instanceof Comparable) {
            return ((Comparable) o1.getValue()).compareTo(o2.getValue());
        }
        throw new UnsupportedOperationException("值的类型尚未实现Comparable接口");
    };


    private static Comparator<Map.Entry> comparatorByValueDesc = (Map.Entry o1, Map.Entry o2) -> {
        if (o1.getValue() instanceof Comparable) {
            return ((Comparable) o2.getValue()).compareTo(o1.getValue());
        }
        throw new UnsupportedOperationException("值的类型尚未实现Comparable接口");
    };

    /**
     * 按键升序排列
     */
    public static <K, V> Map<K, V> sortByKeyAsc(Map<K, V> originMap) {
        if (originMap == null) {
            return null;
        }
        return sort(originMap, comparatorByKeyAsc);
    }

    /**
     * 按键降序排列
     */
    public static <K, V> Map<K, V> sortByKeyDesc(Map<K, V> originMap) {
        if (originMap == null) {
            return null;
        }
        return sort(originMap, comparatorByKeyDesc);
    }


    /**
     * 按值升序排列
     */
    public static <K, V> Map<K, V> sortByValueAsc(Map<K, V> originMap) {
        if (originMap == null) {
            return null;
        }
        return sort(originMap, comparatorByValueAsc);
    }

    /**
     * 按值降序排列
     */
    public static <K, V> Map<K, V> sortByValueDesc(Map<K, V> originMap) {
        if (originMap == null) {
            return null;
        }
        return sort(originMap, comparatorByValueDesc);
    }

    private static <K, V> Map<K, V> sort(Map<K, V> originMap, Comparator<Map.Entry> comparator) {
        return originMap.entrySet()
                .stream()
                .sorted(comparator)
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }
	/**
	    * 求Map<K,V>中Value(值)的最小值
	    *
	    * @param map
	    * @return
	    */
	
	public static Object getMinValue(Map<Integer, Double> map) {
	    if (map == null)
	        return null;
	    Collection<Double> c = map.values();//
	    Object[] obj = c.toArray();
	    Arrays.sort(obj);
	    return obj[0];
	}
	/**
	 * 求Map<K,V>中Value(值)的最大值
	 *
	 * @param map
	 * @return
	 */
	public static Object getMaxValue(Map<Integer, Double> map) {
	    if (map == null)
	        return null;
	    int length =map.size();
	    Collection<Double> c = map.values();
	    Object[] obj = c.toArray();
	    Arrays.sort(obj);
	    return obj[length-1];
	}
	
	/**
	 * 将map进行标准归一化归一化
	 *
	 * @param originMap
	 * @return map
	 */
	public static Map<Integer,Double> MapNormalization(Map<Integer,Double> originMap) {
        if (originMap == null) {
            return null;
        }else {
            double Standard_Deviation = calculateStandardDeviation(originMap);
            double Mean = calculateStandardDeviation(originMap);
        	double Max_X = (double)MapSortUtil.getMaxValue(originMap);
			double Min_X = (double)MapSortUtil.getMinValue(originMap);
			Iterator<Map.Entry<Integer,Double>> iterator=originMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry temp = iterator.next();//HostID--相关系数
				temp.setValue( ((double)temp.getValue() - Mean)/Standard_Deviation);
				originMap.replace((int)temp.getKey(), (double)temp.getValue());
			}
        }
        return originMap;
    }
    /**
     * 将map的标准差求出
     *
     * @param originmap
     * @return map
     */
    public static double calculateStandardDeviation(Map<Integer, Double> originmap) {
        double sum = 0.0;
        double mean = 0.0;
        double sd = 0.0;
        int length = originmap.size();
        for (double value : originmap.values()) {
            sum += value;
        }
        mean = sum / length;
        for (double value : originmap.values()) {
            sd += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sd / length);
    }
    /**
     * 将map的标准差求出
     *
     * @param originmap
     * @return map
     */
    public static double calculateMean(Map<Integer, Double> originmap) {
        double sum = 0.0;
        double mean = 0.0;
        int length = originmap.size();
        for (double value : originmap.values()) {
            sum += value;
        }
        mean = sum / length;

        return mean;
    }


}
