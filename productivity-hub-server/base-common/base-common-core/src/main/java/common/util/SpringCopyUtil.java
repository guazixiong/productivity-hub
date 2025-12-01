package common.util;

import org.springframework.beans.BeanUtils;

/**
 * 类属性赋值转换工具类.
 *
 * @author: pangdi
 * @date: 2023/8/29 11:37
 * @version: 1.0
 */
public class SpringCopyUtil {

    /**
     * 转换源类为目标类.
     *
     * @param source      源数据
     * @param targetClass 目标类
     * @param <T>         源类型
     * @param <U>         目标类型
     * @return 目标类
     */
    public static <T, U> U convert(T source, Class<U> targetClass) {
        try {
            U target = targetClass.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("转换源类为目标类失败");
        }
    }
}
