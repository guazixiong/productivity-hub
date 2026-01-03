package com.pbad.thirdparty.api.shortlink.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * URL验证工具类.
 *
 * @author pbad
 */
public class UrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://([\\w-]+\\.)+[\\w-]+(/[\\w-._~:/?#\\[\\]@!$&'()*+,;=%]*)?$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 验证URL格式是否有效.
     *
     * @param url URL字符串
     * @return 是否有效
     */
    public static boolean isValid(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        // 基本格式验证
        if (!URL_PATTERN.matcher(url.trim()).matches()) {
            return false;
        }

        // 使用Java URL类进行深度验证
        try {
            URL urlObj = new URL(url.trim());
            String protocol = urlObj.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                return false;
            }
            if (urlObj.getHost() == null || urlObj.getHost().isEmpty()) {
                return false;
            }
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * 验证并规范化URL.
     *
     * @param url URL字符串
     * @return 规范化后的URL，如果无效则返回null
     */
    public static String validateAndNormalize(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        String normalized = url.trim();

        // 如果没有协议，默认添加https://
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        if (!isValid(normalized)) {
            return null;
        }

        return normalized;
    }
}

