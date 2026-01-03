package com.pbad.home.service;

import com.pbad.thirdparty.api.DailyQuoteApi;
import com.pbad.thirdparty.api.WeatherApi;

/**
 * 首页服务接口.
 * 管理天气位置信息和每日一签的缓存.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HomeService {

    /**
     * 获取用户的天气信息（从缓存或API获取）.
     *
     * @param userId 用户ID
     * @param latitude 纬度（可选）
     * @param longitude 经度（可选）
     * @param cityName 城市名称（可选）
     * @param ip IP地址（可选，优先使用）
     * @return 天气信息
     */
    WeatherApi.WeatherInfo getWeather(String userId, Double latitude, Double longitude, String cityName, String ip);

    /**
     * 获取用户的每日一签（从缓存或API获取）.
     *
     * @param userId 用户ID
     * @return 每日一签信息
     */
    DailyQuoteApi.DailyQuote getDailyQuote(String userId);

    /**
     * 预加载用户的天气和每日一签缓存（登录时调用）.
     *
     * @param userId 用户ID
     * @param latitude 纬度（可选）
     * @param longitude 经度（可选）
     * @param cityName 城市名称（可选）
     * @param ip IP地址（可选，优先使用）
     */
    void preloadUserCache(String userId, Double latitude, Double longitude, String cityName, String ip);

    /**
     * 强制刷新用户的天气信息（调用第三方API并更新缓存）.
     *
     * @param userId 用户ID
     * @param latitude 纬度（可选）
     * @param longitude 经度（可选）
     * @param cityName 城市名称（可选）
     * @param ip IP地址（可选，优先使用）
     * @return 天气信息
     */
    WeatherApi.WeatherInfo refreshWeather(String userId, Double latitude, Double longitude, String cityName, String ip);

    /**
     * 强制刷新用户的每日一签（调用第三方API并更新缓存）.
     *
     * @param userId 用户ID
     * @return 每日一签信息
     */
    DailyQuoteApi.DailyQuote refreshDailyQuote(String userId);
}

