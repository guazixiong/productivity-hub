package common.util.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * RSA加密算法.
 *
 * @author: pangdi
 * @date: 2023/9/7 14:51
 * @version: 1.0
 */
public class RsaCryptoUtil {

    /**
     * 加密算法和填充方式
     */
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * 签名算法
     */
    private static final String SIGNTYPE = "SHA256withRSA";

    /**
     * 使用RSA算法对消息进行加密.
     *
     * @param message     要加密的消息
     * @param certificate 加密所需的证书
     * @return 加密后的密文
     * @throws IllegalBlockSizeException 加密原串的长度不能超过214字节
     */
    public static String encryptOAEP(String message, X509Certificate certificate) throws IllegalBlockSizeException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(ciphertext);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的证书", e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalBlockSizeException("加密原串的长度不能超过214字节");
        }
    }

    /**
     * 使用RSA算法对密文进行解密.
     *
     * @param ciphertext 密文
     * @param privateKey 解密所需的私钥
     * @return 解密后的明文
     * @throws BadPaddingException 解密失败
     */
    public static String decryptOAEP(String ciphertext, PrivateKey privateKey) throws BadPaddingException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = Base64.getDecoder().decode(ciphertext);
            return new String(cipher.doFinal(data), StandardCharsets.UTF_8);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA v1.5/OAEP", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("无效的私钥", e);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new BadPaddingException("解密失败");
        }
    }
}
