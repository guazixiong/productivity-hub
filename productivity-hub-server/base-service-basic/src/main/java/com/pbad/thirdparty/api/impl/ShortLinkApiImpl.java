package com.pbad.thirdparty.api.impl;

import com.pbad.thirdparty.api.ShortLinkApi;
import com.pbad.thirdparty.api.shortlink.util.UrlValidator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 短链服务API实现类.
 *
 * @author pbad
 */
@Slf4j
@Service
public class ShortLinkApiImpl implements ShortLinkApi {

    /**
     * Short.io API 地址.
     */
    @Value("${shortlink.shortio.api.url:https://api.short.io/links}")
    private String shortIoApiUrl;

    /**
     * Short.io API Key.
     * 默认使用用户提供的密钥，可通过配置覆盖：shortlink.shortio.api.key
     */
    @Value("${shortlink.shortio.api.key:sk_wVYaVbwwUICsz214}")
    private String shortIoApiKey;

    /**
     * Short.io 域名（可选）。
     * 如果配置了，则作为固定 domain 传给 Short.io；否则会自动获取账户的第一个域名。
     * 默认使用 pbad.short.gy
     */
    @Value("${shortlink.shortio.domain:pbad.short.gy}")
    private String shortIoDomain;

    @Value("${shortlink.shortio.timeout:5000}")
    private Integer shortIoTimeout;

    /**
     * 缓存的默认域名（当未配置 shortIoDomain 时使用）。
     */
    private final AtomicReference<String> cachedDefaultDomain = new AtomicReference<>();

    /**
     * HttpClient 实例（复用，线程安全）。
     */
    private final CloseableHttpClient httpClient;

    public ShortLinkApiImpl() {
        // 创建 HttpClient，配置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @PreDestroy
    public void destroy() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            log.error("关闭 HttpClient 失败", e);
        }
    }

    @Override
    public String createShortLink(String originalUrl) {
        // 新规则：
        // - 使用 Short.io 生成短链；
        // - 如果调用失败，则返回原始 URL；
        // - 不使用本地短链，不返回 null；
        // - 每次调用结果互不影响，不做全局熔断。
        if (!StringUtils.hasText(originalUrl)) {
            return originalUrl;
        }
        try {
            String normalizedUrl = UrlValidator.validateAndNormalize(originalUrl);
            if (!StringUtils.hasText(normalizedUrl)) {
                return originalUrl;
            }
            String shortUrl = callShortIo(normalizedUrl);
            if (!StringUtils.hasText(shortUrl)) {
                return normalizedUrl;
            }
            return shortUrl;
        } catch (Exception e) {
            log.error("调用 Short.io 创建短链失败，返回原始URL: {}", originalUrl, e);
            return originalUrl;
        }
    }

    @Override
    public java.util.List<String> batchCreateShortLink(java.util.List<String> originalUrls) {
        java.util.List<String> results = new java.util.ArrayList<>();
        if (originalUrls == null || originalUrls.isEmpty()) {
            return results;
        }
        for (String url : originalUrls) {
            // createShortLink 自身保证不返回 null
            results.add(createShortLink(url));
        }
        return results;
    }

    /**
     * 调用 Short.io 生成短链，失败时返回原始 URL。
     */
    private String callShortIo(String originalUrl) {
        try {
            // 获取要使用的域名（配置的域名或默认域名）
            String domainToUse = getDomainToUse();

            JSONObject body = new JSONObject();
            // 只有在获取到域名时才设置 domain 参数
            // 如果未配置域名且无法获取，尝试不传 domain 参数，让 Short.io 使用默认域名
            if (StringUtils.hasText(domainToUse)) {
                body.put("domain", domainToUse);
            }
            body.put("originalURL", originalUrl);
            body.put("allowDuplicates", true);

            HttpPost httpPost = new HttpPost(shortIoApiUrl);
            httpPost.setHeader("accept", "application/json");
            httpPost.setHeader("content-type", "application/json");
            if (StringUtils.hasText(shortIoApiKey)) {
                httpPost.setHeader("Authorization", shortIoApiKey);
            }

            StringEntity entity = new StringEntity(body.toJSONString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            // 设置超时时间
            int timeout = shortIoTimeout != null ? shortIoTimeout : 5000;
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .build();
            httpPost.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    String respBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    if (StringUtils.hasText(respBody)) {
                        JSONObject json = JSONObject.parseObject(respBody);
                        String shortUrl = json.getString("shortURL");
                        if (StringUtils.hasText(shortUrl)) {
                            return shortUrl;
                        }
                    }
                } else {
                    String bodyStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.warn("Short.io 返回非 2xx 状态码, status={}, body={}", status, bodyStr);
                }
            }
        } catch (IOException e) {
            log.error("调用 Short.io 失败，返回原始URL。originalUrl={}", originalUrl, e);
        } catch (Exception e) {
            log.error("调用 Short.io 失败，返回原始URL。originalUrl={}", originalUrl, e);
        }
        return originalUrl;
    }

    /**
     * 获取要使用的域名。
     * 如果配置了 shortIoDomain，直接使用；否则获取账户的第一个域名。
     */
    private String getDomainToUse() {
        // 如果配置了域名，直接使用
        if (StringUtils.hasText(shortIoDomain)) {
            return shortIoDomain;
        }

        // 尝试使用缓存的默认域名
        String cached = cachedDefaultDomain.get();
        if (StringUtils.hasText(cached)) {
            return cached;
        }

        // 获取账户的第一个域名并缓存
        try {
            String defaultDomain = fetchDefaultDomain();
            if (StringUtils.hasText(defaultDomain)) {
                cachedDefaultDomain.set(defaultDomain);
                return defaultDomain;
            }
        } catch (Exception e) {
            log.error("获取 Short.io 默认域名失败", e);
        }

        return null;
    }

    /**
     * 从 Short.io API 获取账户的第一个域名。
     */
    private String fetchDefaultDomain() {
        try {
            HttpGet httpGet = new HttpGet("https://api.short.io/domains");
            httpGet.setHeader("accept", "application/json");
            if (StringUtils.hasText(shortIoApiKey)) {
                httpGet.setHeader("Authorization", shortIoApiKey);
            }

            // 设置超时时间
            int timeout = shortIoTimeout != null ? shortIoTimeout : 5000;
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(timeout)
                    .setSocketTimeout(timeout)
                    .setConnectionRequestTimeout(timeout)
                    .build();
            httpGet.setConfig(requestConfig);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    String respBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    if (StringUtils.hasText(respBody)) {
                        JSONObject json = JSONObject.parseObject(respBody);
                        // Short.io API 返回格式可能是 {"domains": [...]} 或直接是数组
                        Object domainsObj = json.get("domains");
                        if (domainsObj == null) {
                            // 尝试直接解析为数组
                            try {
                                domainsObj = JSON.parseArray(respBody);
                            } catch (Exception e) {
                                log.debug("解析域名列表为数组失败，尝试其他格式", e);
                            }
                        }
                        
                        if (domainsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> domains = (List<Object>) domainsObj;
                            if (!domains.isEmpty()) {
                                Object firstDomainObj = domains.get(0);
                                JSONObject firstDomain;
                                if (firstDomainObj instanceof JSONObject) {
                                    firstDomain = (JSONObject) firstDomainObj;
                                } else {
                                    firstDomain = JSONObject.parseObject(firstDomainObj.toString());
                                }
                                String domain = firstDomain.getString("hostname");
                                if (StringUtils.hasText(domain)) {
                                    log.info("成功获取 Short.io 默认域名: {}", domain);
                                    return domain;
                                }
                            }
                        }
                    }
                } else {
                    String bodyStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.warn("获取 Short.io 域名列表失败, status={}, body={}", status, bodyStr);
                }
            }
        } catch (IOException e) {
            log.error("调用 Short.io 获取域名列表异常", e);
        } catch (Exception e) {
            log.error("调用 Short.io 获取域名列表异常", e);
        }
        return null;
    }
}


