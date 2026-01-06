package common.util.judge;

import common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 校验参数工具类.
 *
 * @author: pbad
 * @date: 2023/8/30 15:43
 * @version: 1.0
 */
public class JudgeParameterUtil {

    /**
     * 此方法检查输入对象是否为 null，如果为 null，则抛出具有指定代码和错误消息的 BusinessException。
     *
     * @param t        要检查是否为 null 的对象。
     * @param code     如果对象为 null，则将包含在 BusinessException 中的代码。
     * @param errorMsg 如果对象为 null，则将包含在 BusinessException 中的错误消息。
     */
    public static <T> void checkNotNull(T t, String code, String errorMsg) {
        if (t == null) {
            throw new BusinessException(code, errorMsg);
        }
    }

    /**
     * 此方法检查输入字符串是否为 null 或为空，如果是，则抛出 IllegalArgumentException。
     *
     * @param str      要检查的字符串。
     * @param code     如果对象为 null，则将包含在 BusinessException 中的代码。
     * @param errorMsg 如果对象为 null，则将包含在 BusinessException 中的错误消息。
     * @throws IllegalArgumentException 如果输入字符串为 null 或为空。
     */
    public static void checkNotNullOrEmpty(String str, String code, String errorMsg) {
        if (isNullOrEmpty(str)) {
            throw new BusinessException(code, errorMsg);
        }
    }

    /**
     * 此方法检查输入字符串是否为 null 或为空。
     *
     * @param str 要检查的字符串。
     * @return 如果输入字符串为 null 或为空，则返回 true；否则返回 false。
     */
    public static boolean isNullOrEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 判断List集合是否为null或为空。
     *
     * @param list 要判断的List集合
     * @param <T>  List中元素的类型
     * @return 如果List为null或为空，返回true；否则返回false。
     */
    public static <T> boolean checkListIsNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 判断List集合是否为null或为空。
     *
     * @param list     要判断的List集合
     * @param code     错误码
     * @param errorMsg 错误信息
     */
    public static void checkListNullOrEmptyThrowException(List list, String code, String errorMsg) {
        if (checkListIsNullOrEmpty(list)) {
            throw new BusinessException(code, errorMsg);
        }
    }
}
