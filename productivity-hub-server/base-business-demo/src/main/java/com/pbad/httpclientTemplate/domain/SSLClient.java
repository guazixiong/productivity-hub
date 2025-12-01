package com.pbad.httpclientTemplate.domain;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * SSLClient 类用于创建一个自定义的HTTP客户端，该客户端接受所有SSL证书。
 * 这个类继承自 DefaultHttpClient，并重写了其构造方法以实现自定义的SSL处理逻辑。
 * 通常用于测试环境，或者在需要信任所有证书的特定场景下使用。
 *
 * @author: pangdi
 * @date: 2023/12/27 17:07
 * @version: 1.0
 */
public class SSLClient extends DefaultHttpClient {

    /**
     * 构造一个新的SSLClient。
     * 在这个构造方法中，初始化了一个自定义的SSLContext，并使用了一个X509TrustManager，
     * 它不执行任何证书验证。这意味着客户端将信任所有的SSL证书。
     *
     * @throws Exception 如果在创建SSLContext时发生错误。
     */
    public SSLClient() throws Exception {
        super();
        // 创建TLS类型的SSLContext对象
        SSLContext ctx = SSLContext.getInstance("TLS");

        // 创建一个X509TrustManager，它不对证书进行校验
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        // 初始化SSLContext
        ctx.init(null, new TrustManager[]{tm}, null);

        // 创建SSLSocketFactory，允许所有主机名验证
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        // 获取连接管理器并注册自定义的SSLSocketFactory
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
    }
}
