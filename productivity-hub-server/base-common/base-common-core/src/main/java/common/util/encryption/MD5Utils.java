package common.util.encryption;

import java.security.MessageDigest;

/**
 * MD5工具类，提供MD5加密和解密功能.
 *
 * @author: pbad
 * @date: 2023/9/7 14:58
 * @version: 1.0
 */
public class MD5Utils {

    /**
     * 将输入字符串进行MD5加密，生成32位MD5码。
     *
     * @param inStr 待加密的字符串
     * @return 生成的32位MD5码
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuilder hexValue = new StringBuilder();
        for (byte md5Byte : md5Bytes) {
            int val = (md5Byte) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /**
     * 验签。
     *
     * @param inStr  待解密的字符串
     * @param hexStr 加密字符串
     * @return 是否验签通过
     */
    public static boolean verifyMD5(String inStr, String hexStr) {
        return string2MD5(inStr).equals(hexStr);
    }
}
