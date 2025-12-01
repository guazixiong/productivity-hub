package common.util.encryption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 证书工具类.
 *
 * @author: pangdi
 * @date: 2023/9/7 14:50
 * @version: 1.0
 */
public class PemUtil {

    /**
     * 加载给定的私钥字符串并返回对应的 {@link PrivateKey} 对象。
     *
     * @param privateKey 包含私钥信息的字符串
     * @return 与给定字符串对应的私钥对象
     */
    public static PrivateKey loadPrivateKey(String privateKey) {
        privateKey = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }

    /**
     * 从给定的输入流中加载私钥并返回对应的 {@link PrivateKey} 对象。.
     *
     * @param inputStream 包含私钥信息的输入流
     * @return 与给定输入流中的私钥信息对应的私钥对象
     */
    public static PrivateKey loadPrivateKey(InputStream inputStream) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        byte[] buffer = new byte[1024];
        String privateKey;
        try {
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                os.write(buffer, 0, length);
            }
            privateKey = os.toString("UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("无效的密钥", e);
        }
        return loadPrivateKey(privateKey);
    }

    /**
     * 从给定的输入流中加载证书并返回对应的 {@link X509Certificate} 对象。
     *
     * @param inputStream 包含证书信息的输入流
     * @return 与给定输入流中的证书信息对应的证书对象
     */
    public static X509Certificate loadCertificate(InputStream inputStream) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
            cert.checkValidity();
            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("证书已过期", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("证书尚未生效", e);
        } catch (CertificateException e) {
            throw new RuntimeException("无效的证书", e);
        }
    }

    /**
     * 从给定的输入流中加载公钥并返回对应的 {@link PublicKey} 对象。.
     *
     * @param inputStream 包含公钥信息的输入流
     * @return 与给定输入流中的公钥信息对应的私钥对象
     */
    public static PublicKey loadPublicKey(InputStream inputStream) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
        byte[] buffer = new byte[1024];
        String publicKey;
        try {
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                os.write(buffer, 0, length);
            }
            publicKey = os.toString("UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("无效的密钥", e);
        }
        return loadPublicKey(publicKey);
    }

    /**
     * 加载给定的公钥字符串并返回对应的 {@link PublicKey} 对象。
     *
     * @param publicKey 包含公钥信息的字符串
     * @return 与给定字符串对应的公钥对象
     */
    public static PublicKey loadPublicKey(String publicKey) {
        publicKey = publicKey
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(publicKey)));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("当前Java环境不支持RSA", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("无效的密钥格式");
        }
    }
}
