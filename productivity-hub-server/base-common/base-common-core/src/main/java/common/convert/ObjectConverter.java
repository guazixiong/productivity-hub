package common.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;

/**
 * Object对象转换工具类.
 *
 * @author: pbad
 * @date: 2023/9/19 17:22
 * @version: 1.1
 */
public class ObjectConverter {

    /**
     * 将json字符串转化为对象.
     *
     * @param json  json字符串
     * @param clazz clazz对象
     * @param <T>   T
     * @return T
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(JSONObject.toJSONString(json), clazz);
    }

    /**
     * 将对象转换为json字符串.
     *
     * @param object 对象
     * @param <T>    T
     * @return json字符串
     */
    public static <T> String toJson(T object) {
        return JSON.toJSONString(object);
    }

    /**
     * 将Object对象转换为指定的Class对象
     *
     * @param source      要转换的Object对象
     * @param targetClass 目标Class对象
     * @param <T>         目标Class的类型
     * @return 转换后的对象
     */
    public static <T> T objectToClass(Object source, Class<T> targetClass) {
        String jsonString = JSON.toJSONString(source);
        return JSON.parseObject(jsonString, targetClass);
    }

    /**
     * 将字符串转换为BigDecimal，不保留小数位，为空时返回0
     *
     * @param inputString 输入的字符串
     * @return 转换后的BigDecimal
     */
    public static BigDecimal convertToBigDecimalOrZero(String inputString) {
        if (inputString != null && !inputString.isEmpty()) {
            return new BigDecimal(inputString);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将字符串转换为BigDecimal，保留指定小数位，为空时返回0
     *
     * @param inputString 输入的字符串
     * @param scale       保留的小数位数
     * @return 转换后的BigDecimal
     */
    public static BigDecimal convertToBigDecimalWithScaleOrZero(String inputString, int scale) {
        if (inputString != null && !inputString.isEmpty()) {
            BigDecimal bigDecimalValue = new BigDecimal(inputString);
            return bigDecimalValue.setScale(scale, BigDecimal.ROUND_HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 将字符串转换为BigDecimal，不保留小数位
     *
     * @param inputString 输入的字符串
     * @return 转换后的BigDecimal
     */
    public static BigDecimal stringToBigDecimal(String inputString) {
        if (inputString != null && !inputString.isEmpty()) {
            return new BigDecimal(inputString);
        } else {
            throw new IllegalArgumentException("输入字符串为空或null");
        }
    }

    /**
     * 将字符串转换为BigDecimal，保留指定小数位
     *
     * @param inputString 输入的字符串
     * @param scale       保留的小数位数
     * @return 转换后的BigDecimal
     */
    public static BigDecimal stringToBigDecimalWithScale(String inputString, int scale) {
        if (inputString != null && !inputString.isEmpty()) {
            BigDecimal bigDecimalValue = new BigDecimal(inputString);
            return bigDecimalValue.setScale(scale, BigDecimal.ROUND_HALF_UP);
        } else {
            throw new IllegalArgumentException("输入字符串为空或null");
        }
    }
}
