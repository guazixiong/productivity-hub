package common.util;

import java.math.BigDecimal;

/**
 * 初始化工具类.
 *
 * @author: pangdi
 * @date: 2023/11/1 16:34
 * @version: 1.0
 */
public class InitUtil {

    /**
     * 此方法检查输入对象是否为 null，如果为 null，则使用指定类的实例进行初始化。
     *
     * @param t   要检查是否为 null 的对象。
     * @param clz 如果对象为 null，则要实例化的类。
     * @param <T> 对象和类的类型。
     * @return 如果对象不为 null，则返回输入对象；如果为 null，则返回指定类的新实例。
     */
    public static <T> T checkNullToInitClass(T t, Class<T> clz) {
        try {
            if (t == null) {
                t = clz.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 将可能为null的BigDecimal对象转换为零，以防止空指针异常。
     *
     * @param source 可能为null的BigDecimal对象
     * @return 如果source为null，则返回BigDecimal.ZERO；否则返回source本身
     */
    public static BigDecimal handlerNullToZero(BigDecimal source) {
        return source == null ? BigDecimal.ZERO : source;
    }
}
