package common.util.encryption;

import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * DES加密/解密工具类，提供对字符串的DES加密和解密操作。
 *
 * <p>加密算法使用DES/CBC/PKCS5Padding。</p>
 * <p>注意：秘钥的长度必须是8的倍数。</p>
 *
 * <p>
 * 使用示例：
 * <pre>
 * // 加密
 * String encrypted = DesUtil.nonNullEncryptDESCBC("Hello World", "mySecretKey");
 *
 * // 解密
 * String decrypted = DesUtil.decryptDESCBC(encrypted, "mySecretKey");
 * </pre>
 * </p>
 *
 * @author: pbad
 * @date: 2023/9/7 15:00
 * @version: 1.0
 */
public class DesUtil {
    private static final String IV = "union968";

    /**
     * 对非空字符串进行DES加密。
     *
     * @param src 数据源
     * @param key 秘钥，长度必须是8的倍数
     * @return 加密后的数据，如果输入为空则返回null
     * @throws Exception
     */
    public static String nonNullEncryptDESCBC(final String src, final String key) throws Exception{
        if (!StringUtils.isEmpty(src)) {
            return encryptDESCBC(src,key);
        }
        return null;
    }

    /**
     * 对字符串进行DES加密。
     *
     * @param src 数据源
     * @param key 秘钥，长度必须是8的倍数
     * @return 加密后的数据
     * @throws Exception
     */
    public static String encryptDESCBC(final String src, final String key) throws Exception{
        // 生成key,同时制定是des还是DESede,两者的key长度要求不同.
        final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        // 加密向量
        final IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        final Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,iv);
        // 通过base64,将加密数组转换成字符串
        final byte[] b = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(b);
    }

    /**
     * 对字符串进行DES解密。
     *
     * @param src 数据源
     * @param key 秘钥
     * @return 解密后的数据
     * @throws Exception
     */
    public static String decryptDESCBC(final String src, final String key) throws Exception {
        // 通过base64,将字符串转换成byte数组
        final byte[] bytes = Base64.getDecoder().decode(src);
        // 解密key
        final DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(StandardCharsets.UTF_8));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        // 向量
        final IvParameterSpec iv = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
        final Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,secretKey,iv);
        final byte[] retByte = cipher.doFinal(bytes);
        return new String(retByte);
    }
}
