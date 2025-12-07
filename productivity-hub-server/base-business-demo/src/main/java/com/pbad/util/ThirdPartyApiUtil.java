package com.pbad.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 第三方API工具类.
 * <p>提供天气、每日一言等第三方API的调用方法，方便各模块复用.</p>
 *
 * @author pbad
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ThirdPartyApiUtil {

    /**
     * 天气API（使用免费的天气API）
     */
    private static final String WEATHER_API_URL_TEMPLATE = "https://api.vvhan.com/api/weather?city=%s";

    /**
     * 每日一言API
     */
    private static final String DAILY_QUOTE_API_URL = "https://v1.hitokoto.cn/";

    /**
     * 获取指定城市的天气信息.
     *
     * @param city 城市名称，如：郑州、北京、上海等
     * @return 天气信息，如果获取失败返回默认值
     */
    public static WeatherInfo getWeatherInfo(String city) {
        if (isBlank(city)) {
            city = "郑州";
        }
        try {
            String url = String.format(WEATHER_API_URL_TEMPLATE, city);
            String body = HttpUtil.get(url, 5000);
            JSONObject obj = JSON.parseObject(body);
            if (obj != null) {
                // 尝试解析不同的API响应格式
                JSONObject data = obj.getJSONObject("data");
                if (data != null) {
                    String cityName = firstNonBlank(data.getString("city"), city);
                    String weather = firstNonBlank(data.getString("weather"), data.getString("type"), "未知");
                    String temp = firstNonBlank(data.getString("temp"), data.getString("temperature"), "未知");
                    String wind = firstNonBlank(data.getString("wind"), data.getString("windDir"), "未知");
                    String humidity = firstNonBlank(data.getString("humidity"), data.getString("shidu"), "未知");
                    return new WeatherInfo(cityName, weather, temp, wind, humidity);
                }
                // 如果data为空，尝试直接从根对象获取
                String weather = firstNonBlank(obj.getString("weather"), obj.getString("type"), "未知");
                String temp = firstNonBlank(obj.getString("temp"), obj.getString("temperature"), "未知");
                if (!isBlank(weather) || !isBlank(temp)) {
                    String wind = firstNonBlank(obj.getString("wind"), obj.getString("windDir"), "未知");
                    String humidity = firstNonBlank(obj.getString("humidity"), obj.getString("shidu"), "未知");
                    return new WeatherInfo(city, weather, temp, wind, humidity);
                }
            }
        } catch (Exception e) {
            log.warn("获取{}天气信息失败: {}", city, e.getMessage());
        }
        // 返回默认值
        return new WeatherInfo(city, "未知", "未知", "未知", "未知");
    }

    /**
     * 获取每日一言.
     *
     * @return 每日一言信息，如果获取失败返回默认值
     */
    public static DailyQuote getDailyQuote() {
        try {
            String body = HttpUtil.get(DAILY_QUOTE_API_URL, 5000);
            JSONObject obj = JSON.parseObject(body);
            if (obj != null) {
                String hitokoto = obj.getString("hitokoto");
                String from = obj.getString("from");
                String fromWho = obj.getString("from_who");
                if (isBlank(fromWho)) {
                    fromWho = from;
                }
                if (isBlank(fromWho)) {
                    fromWho = "未知";
                }
                if (!isBlank(hitokoto)) {
                    return new DailyQuote(hitokoto, fromWho);
                }
            }
        } catch (Exception e) {
            log.warn("获取每日一言失败: {}", e.getMessage());
        }
        // 返回默认值
        return new DailyQuote("快谈对象!!!!", "未知");
    }

    /**
     * 从多个值中返回第一个非空值.
     */
    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * 判断字符串是否为空.
     */
    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * 天气信息数据类.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherInfo {
        private String city;
        private String weather;
        private String temp;
        private String wind;
        private String humidity;
    }

    /**
     * 每日一言数据类.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailyQuote {
        private String quote;
        private String from;
    }
}

