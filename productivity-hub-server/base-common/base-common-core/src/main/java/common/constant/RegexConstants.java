package common.constant;

/**
 * 公共正则.
 *
 * @author: pangdi
 * @date: 2023/9/12 10:05
 * @version: 1.1
 */
public interface RegexConstants {

    /**
     * 身份证号正则
     */
    String ID_CARD_REGEX = "^[1-9]\\d{5}(18|19|20|(3\\d))\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";

    /**
     * 手机号码校验规则
     */
    String PHONE_PATTERN = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][0-9])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$";

    /**
     * 昵称 汉字，字母，数字，下划线 中英文括号 长度不限
     */
    String NICK_NAME_PATTERN = "^[\\u4E00-\\u9FA5A-Za-z0-9_()（）]+$";

    /**
     * 电话号码的正则
     */
    String TELEPHONE_PATTERN = "^(0\\d{2}-\\d{8}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$";

    /**
     * 邮箱的校验正则
     */
    String EMAIL_PATTERN = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 纯数字的正则
     */
    String NUMBER_PATTERN = "^[0-9][0-9]*$";

    /**
     * 年龄的正则
     */
    String AGE_PATTERN = "^(([0-9]|[1-9][0-9]|1[0-7][0-9])(\\\\.[0-9]+)?|180)$";

    /**
     * 金额的正则
     */
    String AMOUNT_PATTERN = "^([1-9]\\d{0,9}|0)([.]?|(\\.\\d{1,2})?)$";

    /**
     * 手机和电话合用的正则
     */
    String TELE_PHONE_PATTERN = "^([1]\\d{10}|([\\(（]?0[0-9]{2,3}[）\\)]?[-]?)?([2-9][0-9]{6,7})+(\\-[0-9]{1,4})?)$";

    /**
     * 备注正则
     */
    String REMARK_PATTERN = "^[\\S]{0,300}$";

    /**
     * 其他输入正则
     */
    String OTHER_INPUT_PATTERN = "^[\\S]{0,50}$";

    /**
     * 银行卡正则
     */
    String BANK_CARD_PATTERN = "^[1-9][\\d]{14,18}$";

    /**
     * 数量正则
     */
    String NORMAL_NUMBER_PATTERN = "^[0-9]|[1-9][\\d]{0,19}$";

    /**
     * 是否存在中文字符
     */
    public static final String CHINESE_CHAR_REGEX = ".*[\\u4e00-\\u9fa5].*";

    /**
     * 是否存在"换行符"等不可见字符
     */
    public static final String NON_VISIBLE_CHARACTERS_REGEX = ".*[\\p{Cntrl}].*";
}
