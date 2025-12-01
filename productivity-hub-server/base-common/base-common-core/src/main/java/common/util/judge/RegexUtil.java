package common.util.judge;

import common.constant.RegexConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类.
 *
 * @author: pangdi
 * @date: 2023/10/19 18:03
 * @version: 1.0
 */
public class RegexUtil {

    /**
     * 验证身份证号码是否合规
     *
     * @param idCard 要验证的身份证号码
     * @return 如果身份证号码合规，返回true；否则返回false
     */
    public static Boolean isValidIdCard(String idCard) {
        Pattern pattern = Pattern.compile(RegexConstants.ID_CARD_REGEX);
        Matcher matcher = pattern.matcher(idCard);
        return matcher.matches();
    }
}
