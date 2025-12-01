package common.util;

import common.util.judge.RegexUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 身份证信息提取工具类
 *
 * @author: pangdi
 * @date: 2023-10-8 11:21:49
 * @version: 1.0
 */
public class IDCardUtil {

    /**
     * 从身份证号中提取生日信息
     *
     * @param idCard 身份证号
     * @return 生日日期对象
     * @throws ParseException 如果身份证格式不正确
     */
    public static Date extractBirthday(String idCard) throws ParseException {
        if (RegexUtil.isValidIdCard(idCard)) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }
        String birthdayStr = idCard.substring(6, 14);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.parse(birthdayStr);
    }

    /**
     * 从身份证号中提取性别
     *
     * @param idCard 身份证号
     * @return 性别（男/女）
     * @throws IllegalArgumentException 如果身份证号格式不正确
     */
    public static String extractGender(String idCard) {
        if (RegexUtil.isValidIdCard(idCard)) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }
        int genderBit = Integer.parseInt(idCard.substring(16, 17));
        return genderBit % 2 == 0 ? "女" : "男";
    }

    /**
     * 从身份证号中提取生肖
     *
     * @param idCard 身份证号
     * @return 生肖
     * @throws ParseException 如果身份证格式不正确
     */
    public static String extractChineseZodiac(String idCard) throws ParseException {
        Date birthday = extractBirthday(idCard);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthday);
        int year = calendar.get(Calendar.YEAR);
        String[] zodiacs = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
        return zodiacs[(year - 1900) % 12];
    }

    /**
     * 从身份证号中提取星座信息
     *
     * @param idCard 身份证号
     * @return 星座信息
     * @throws ParseException 如果身份证格式不正确
     */
    public static String extractZodiacSign(String idCard) throws ParseException {
        Date birthday = extractBirthday(idCard);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthday);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String[] zodiacSigns = {"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
        int[] daysInMonth = {20, 19, 21, 20, 21, 21, 22, 23, 23, 23, 22, 22, 21};

        int index = month - 1;
        if (day >= daysInMonth[index]) {
            index++;
        }
        return zodiacSigns[index];
    }

    public static void main(String[] args) throws ParseException {
        String idCard = "身份证号码"; // 请替换成实际的身份证号码
        Date birthday = extractBirthday(idCard);
        String gender = extractGender(idCard);
        String zodiac = extractChineseZodiac(idCard);
        String zodiacSign = extractZodiacSign(idCard);
        System.out.println("生日: " + birthday);
        System.out.println("性别: " + gender);
        System.out.println("生肖: " + zodiac);
        System.out.println("星座: " + zodiacSign);
    }
}

