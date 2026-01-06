package com.pbad.thirdparty.api.shortlink.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.shortlink.ShortLinkProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 免费第三方短链服务提供者集合.
 * <p>内置支持 TinyURL、is.gd、shrtco.de 免费接口。</p>
 */
@Slf4j
public class FreeShortLinkProvider implements ShortLinkProvider {

    private static final String PROVIDER_TINYURL = "tinyurl";
    private static final String PROVIDER_ISGD = "isgd";
    private static final String PROVIDER_IS_GD = "is.gd";
    private static final String PROVIDER_SHRTCO = "shrtco";
    private static final String PROVIDER_SHRTCODE = "shrtcode";

    @Override
    public String getProviderName() {
        return "FreeShortLink";
    }

    @Override
    public String createShortLink(String originalUrl, ShortLinkConfig config) throws Exception {
        String provider = normalizeProvider(config != null ? config.getProvider() : null);
        int timeout = config != null ? config.getTimeout() : 5000;

        switch (provider) {
            case PROVIDER_ISGD:
            case PROVIDER_IS_GD:
                return callIsGd(originalUrl, config, timeout);
            case PROVIDER_SHRTCO:
            case PROVIDER_SHRTCODE:
                return callShrtco(originalUrl, config, timeout);
            case PROVIDER_TINYURL:
            default:
                return callTinyUrl(originalUrl, config, timeout);
        }
    }

    private String callTinyUrl(String originalUrl, ShortLinkConfig config, int timeout) throws Exception {
        String apiUrl = StringUtils.hasText(config != null ? config.getApiUrl() : null)
                ? config.getApiUrl()
                : "https://tinyurl.com/api-create.php?url=%s";
        String requestUrl = buildGetUrl(apiUrl, originalUrl);

        HttpResponse response = HttpRequest.get(requestUrl)
                .timeout(timeout)
                .execute();
        String body = response.body();
        validateResponse(response, body, "TinyURL");
        return body.trim();
    }

    private String callIsGd(String originalUrl, ShortLinkConfig config, int timeout) throws Exception {
        String apiUrl = StringUtils.hasText(config != null ? config.getApiUrl() : null)
                ? config.getApiUrl()
                : "https://is.gd/create.php?format=simple&url=%s";
        String requestUrl = buildGetUrl(apiUrl, originalUrl);

        HttpResponse response = HttpRequest.get(requestUrl)
                .timeout(timeout)
                .execute();
        String body = response.body();
        validateResponse(response, body, "is.gd");
        return body.trim();
    }

    private String callShrtco(String originalUrl, ShortLinkConfig config, int timeout) throws Exception {
        String apiUrl = StringUtils.hasText(config != null ? config.getApiUrl() : null)
                ? config.getApiUrl()
                : "https://api.shrtco.de/v2/shorten?url=%s";
        String requestUrl = buildGetUrl(apiUrl, originalUrl);

        HttpResponse response = HttpRequest.get(requestUrl)
                .timeout(timeout)
                .execute();
        String body = response.body();
        validateResponse(response, body, "shrtco.de");

        JSONObject result = JSON.parseObject(body);
        if (result != null && result.getBooleanValue("ok")) {
            JSONObject data = result.getJSONObject("result");
            if (data != null) {
                String url = data.getString("full_short_link");
                if (!StringUtils.hasText(url)) {
                    url = data.getString("short_link");
                }
                if (StringUtils.hasText(url)) {
                    return url;
                }
            }
        }

        throw new RuntimeException("shrtco.de 返回内容无法解析: " + body);
    }

    private String buildGetUrl(String template, String originalUrl) throws Exception {
        String encoded = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8.name());
        if (template.contains("%s")) {
            return String.format(template, encoded);
        }
        return template + encoded;
    }

    private void validateResponse(HttpResponse response, String body, String provider) {
        if (response == null || response.getStatus() >= 400) {
            throw new RuntimeException(provider + " 短链服务请求失败，HTTP状态：" +
                    (response != null ? response.getStatus() : "unknown"));
        }
        if (!StringUtils.hasText(body)) {
            throw new RuntimeException(provider + " 短链服务返回为空");
        }
    }

    private String normalizeProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            return PROVIDER_TINYURL;
        }
        String normalized = provider.toLowerCase();
        switch (normalized) {
            case PROVIDER_TINYURL:
            case PROVIDER_ISGD:
            case PROVIDER_IS_GD:
            case PROVIDER_SHRTCO:
            case PROVIDER_SHRTCODE:
                return normalized;
            default:
                log.warn("未知的免费短链服务商: {}，回退到 tinyurl", provider);
                return PROVIDER_TINYURL;
        }
    }
}


