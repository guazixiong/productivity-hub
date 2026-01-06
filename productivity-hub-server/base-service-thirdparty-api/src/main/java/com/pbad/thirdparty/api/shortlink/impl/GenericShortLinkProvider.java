package com.pbad.thirdparty.api.shortlink.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.shortlink.ShortLinkProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 通用第三方短链服务提供者.
 * <p>支持标准的JSON格式API调用.</p>
 *
 * @author pbad
 */
@Slf4j
public class GenericShortLinkProvider implements ShortLinkProvider {

    @Override
    public String getProviderName() {
        return "Generic";
    }

    @Override
    public String createShortLink(String originalUrl, ShortLinkConfig config) throws Exception {
        if (!StringUtils.hasText(config.getApiUrl())) {
            throw new IllegalArgumentException("API地址不能为空");
        }

        // 构建请求
        HttpRequest request = HttpRequest.post(config.getApiUrl())
                .timeout(config.getTimeout())
                .header("Content-Type", "application/json");

        // 添加认证信息
        if (StringUtils.hasText(config.getApiKey())) {
            request.header("Authorization", "Bearer " + config.getApiKey());
        }

        // 构建请求体（通用格式：{"url": "原始URL"}）
        JSONObject requestBody = new JSONObject();
        requestBody.put("url", originalUrl);

        // 执行请求
        String responseBody = request.body(requestBody.toJSONString())
                .execute()
                .body();

        // 解析响应
        JSONObject response = JSON.parseObject(responseBody);

        // 提取短链URL（支持多种响应格式）
        String shortLinkUrl = extractShortLinkUrl(response);

        if (!StringUtils.hasText(shortLinkUrl)) {
            throw new RuntimeException("第三方短链服务返回的短链URL为空");
        }

        return shortLinkUrl;
    }

    /**
     * 从响应中提取短链URL.
     * 支持多种响应格式：
     * 1. {"short_url": "..."}
     * 2. {"data": {"short_url": "..."}}
     * 3. {"result": {"short_url": "..."}}
     * 4. {"shortUrl": "..."}
     */
    private String extractShortLinkUrl(JSONObject response) {
        if (response == null) {
            return null;
        }

        // 尝试多种字段名
        String[] possibleFields = {"short_url", "shortUrl", "short_link", "shortLink", "url"};
        for (String field : possibleFields) {
            String url = response.getString(field);
            if (StringUtils.hasText(url)) {
                return url;
            }
        }

        // 尝试嵌套结构
        if (response.containsKey("data")) {
            JSONObject data = response.getJSONObject("data");
            if (data != null) {
                for (String field : possibleFields) {
                    String url = data.getString(field);
                    if (StringUtils.hasText(url)) {
                        return url;
                    }
                }
            }
        }

        if (response.containsKey("result")) {
            JSONObject result = response.getJSONObject("result");
            if (result != null) {
                for (String field : possibleFields) {
                    String url = result.getString(field);
                    if (StringUtils.hasText(url)) {
                        return url;
                    }
                }
            }
        }

        return null;
    }
}

