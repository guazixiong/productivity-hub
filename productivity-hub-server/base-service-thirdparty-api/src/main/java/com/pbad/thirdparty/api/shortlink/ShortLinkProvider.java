package com.pbad.thirdparty.api.shortlink;

import org.springframework.util.StringUtils;

/**
 * 第三方短链服务提供者接口.
 * <p>定义统一的短链服务接口，支持多种第三方服务商.</p>
 *
 * @author pbad
 */
public interface ShortLinkProvider {

    /**
     * 获取服务商名称.
     *
     * @return 服务商名称
     */
    String getProviderName();

    /**
     * 创建短链.
     *
     * @param originalUrl 原始URL
     * @param config      配置信息（API地址、密钥等）
     * @return 短链URL
     * @throws Exception 创建失败时抛出异常
     */
    String createShortLink(String originalUrl, ShortLinkConfig config) throws Exception;

    /**
     * 短链服务配置.
     */
    class ShortLinkConfig {
        /**
         * 服务提供商标识，例如: tinyurl、isgd、shrtco、generic.
         */
        private String provider;
        private String apiUrl;
        private String apiKey;
        private String apiSecret;
        private Integer timeout;
        private Integer retryCount;

        public String getProvider() {
            return StringUtils.hasText(provider) ? provider : "generic";
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public Integer getTimeout() {
            return timeout != null ? timeout : 5000;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Integer getRetryCount() {
            return retryCount != null ? retryCount : 3;
        }

        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }
    }
}

