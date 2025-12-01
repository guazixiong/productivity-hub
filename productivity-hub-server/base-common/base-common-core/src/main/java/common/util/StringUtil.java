package common.util;

/**
 * 字符串工具类.
 *
 * @author: pangdi
 * @date: 2023/10/9 14:01
 * @version: 1.0
 */
public class StringUtil {

    /**
     * 将给定对象转换为字符串，每行缩进 4 个空格（除了首行）
     *
     * @param o 对象
     * @return 格式化字符串
     */
    public static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * 将给定的字符转换为相应的整数值。
     *
     * @param character 要转换的字符
     * @return 转换后的整数值
     */
    public static int charToInt(char character) {
        return character - '0';
    }
}
