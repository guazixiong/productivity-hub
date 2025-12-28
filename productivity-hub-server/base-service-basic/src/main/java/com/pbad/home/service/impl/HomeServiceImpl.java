package com.pbad.home.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.home.service.HomeService;
import com.pbad.thirdparty.api.DailyQuoteApi;
import com.pbad.thirdparty.api.WeatherApi;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 首页服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final String WEATHER_CACHE_KEY_PREFIX = "phub:home:weather:";
    private static final String DAILY_QUOTE_CACHE_KEY_PREFIX = "phub:home:dailyQuote:";
    private static final String WEATHER_LOCK_KEY_PREFIX = "phub:home:weather:lock:";
    private static final String DAILY_QUOTE_LOCK_KEY_PREFIX = "phub:home:dailyQuote:lock:";
    private static final String DATE_SEPARATOR = ":";
    private static final long LOCK_EXPIRE_SECONDS = 30;
    private static final int MAX_RETRY_TIMES = 3; // 减少重试次数：从10次减少到3次
    private static final long RETRY_INTERVAL_MS = 100; // 减少重试间隔：从200ms减少到100ms

    private final WeatherApi weatherApi;
    private final DailyQuoteApi dailyQuoteApi;
    private final RedisUtil redisUtil;

    /**
     * 获取今天的日期字符串（格式：yyyy-MM-dd）
     */
    private String getTodayDate() {
        return LocalDate.now().toString();
    }

    /**
     * 计算到次日凌晨的秒数
     */
    private long getSecondsUntilNextDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return ChronoUnit.SECONDS.between(now, nextDay);
    }

    /**
     * 构建天气缓存key
     */
    private String buildWeatherCacheKey(String userId, String date) {
        return WEATHER_CACHE_KEY_PREFIX + userId + DATE_SEPARATOR + date;
    }

    /**
     * 构建每日一签缓存key
     */
    private String buildDailyQuoteCacheKey(String userId, String date) {
        return DAILY_QUOTE_CACHE_KEY_PREFIX + userId + DATE_SEPARATOR + date;
    }

    @Override
    public WeatherApi.WeatherInfo getWeather(String userId, Double latitude, Double longitude, String cityName, String ip) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[HomeService] 用户ID为空，直接调用API获取天气");
            return getWeatherFromApi(latitude, longitude, cityName, ip);
        }

        String today = getTodayDate();
        String cacheKey = buildWeatherCacheKey(userId, today);
        String lockKey = WEATHER_LOCK_KEY_PREFIX + userId + DATE_SEPARATOR + today;
        
        return getCachedData(
            cacheKey,
            lockKey,
            "天气信息",
            WeatherApi.WeatherInfo.class,
            () -> getWeatherFromApi(latitude, longitude, cityName, ip),
            userId
        );
    }

    @Override
    public DailyQuoteApi.DailyQuote getDailyQuote(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[HomeService] 用户ID为空，直接调用API获取每日一签");
            return dailyQuoteApi.getDailyQuote();
        }

        String today = getTodayDate();
        String cacheKey = buildDailyQuoteCacheKey(userId, today);
        String lockKey = DAILY_QUOTE_LOCK_KEY_PREFIX + userId + DATE_SEPARATOR + today;
        
        return getCachedData(
            cacheKey,
            lockKey,
            "每日一签",
            DailyQuoteApi.DailyQuote.class,
            dailyQuoteApi::getDailyQuote,
            userId
        );
    }

    /**
     * 通用的缓存获取方法
     */
    private <T> T getCachedData(String cacheKey, String lockKey, String dataType, 
                                Class<T> clazz, Supplier<T> apiSupplier, String userId) {
        log.info("[HomeService] 尝试从缓存获取{}，用户ID: {}，缓存key: {}", dataType, userId, cacheKey);

        // 先尝试从缓存获取
        Object cached = redisUtil.getValue(cacheKey);
        
        if (cached != null) {
            T result = convertCachedData(cached, clazz, dataType, userId, cacheKey);
            if (result != null) {
                return result;
            }
        } else {
            // 缓存值为null，检查key是否存在（可能是序列化问题）
            Boolean keyExists = redisUtil.hasKey(cacheKey);
            if (keyExists != null && keyExists) {
                log.warn("[HomeService] 缓存key {} 存在但值为null，可能是序列化问题，删除key后重新获取", cacheKey);
                redisUtil.delete(cacheKey);
            }
        }

        // 缓存不存在或解析失败，尝试获取分布式锁
        Boolean lockAcquired = redisUtil.setIfAbsent(lockKey, "loading", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        if (lockAcquired != null && lockAcquired) {
            // 获取锁成功，从API获取
            log.info("[HomeService] 获取锁成功，从API获取用户 {} 的{}", userId, dataType);
            try {
                T data = apiSupplier.get();
                
                // 保存到缓存
                if (data != null) {
                    long expireSeconds = getSecondsUntilNextDay();
                    redisUtil.setKey(cacheKey, data, expireSeconds, TimeUnit.SECONDS);
                    log.info("[HomeService] 用户 {} 的{}已缓存，缓存key: {}，过期时间: {} 秒", userId, dataType, cacheKey, expireSeconds);
                }
                
                redisUtil.delete(lockKey);
                return data;
            } catch (Exception e) {
                redisUtil.delete(lockKey);
                log.error("[HomeService] 获取{}失败: {}", dataType, e.getMessage(), e);
                throw e;
            }
        } else {
            // 获取锁失败，等待后重试
            log.info("[HomeService] 获取锁失败，等待后重试，用户ID: {}", userId);
            return waitAndRetry(cacheKey, lockKey, clazz, apiSupplier, dataType, userId);
        }
    }

    /**
     * 转换缓存数据
     */
    @SuppressWarnings("unchecked")
    private <T> T convertCachedData(Object cached, Class<T> clazz, String dataType, String userId, String cacheKey) {
        try {
            if (clazz.isInstance(cached)) {
                log.info("[HomeService] 从缓存获取用户 {} 的{}，缓存key: {}", userId, dataType, cacheKey);
                return (T) cached;
            } else if (cached instanceof JSONObject) {
                T result = JSON.parseObject(((JSONObject) cached).toJSONString(), clazz);
                log.info("[HomeService] 从缓存（JSONObject）获取用户 {} 的{}，缓存key: {}", userId, dataType, cacheKey);
                return result;
            } else if (cached instanceof LinkedHashMap || cached instanceof Map) {
                String jsonString = JSON.toJSONString(cached);
                T result = JSON.parseObject(jsonString, clazz);
                log.info("[HomeService] 从缓存（Map）获取用户 {} 的{}，缓存key: {}", userId, dataType, cacheKey);
                return result;
            } else {
                String jsonString = JSON.toJSONString(cached);
                T result = JSON.parseObject(jsonString, clazz);
                log.info("[HomeService] 从缓存（通用转换）获取用户 {} 的{}，缓存key: {}，缓存类型: {}", 
                    userId, dataType, cacheKey, cached.getClass().getName());
                return result;
            }
        } catch (Exception e) {
            log.warn("[HomeService] 解析{}缓存失败，缓存类型: {}，错误: {}", 
                dataType, cached != null ? cached.getClass().getName() : "null", e.getMessage());
            return null;
        }
    }

    /**
     * 等待并重试获取数据
     */
    private <T> T waitAndRetry(String cacheKey, String lockKey, Class<T> clazz, 
                               Supplier<T> apiSupplier, String dataType, String userId) {
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            try {
                Thread.sleep(RETRY_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("[HomeService] 等待被中断，用户ID: {}", userId);
                break;
            }

            // 检查锁是否已释放
            Boolean lockExists = redisUtil.hasKey(lockKey);
            if (lockExists == null || !lockExists) {
                // 锁已释放，尝试从缓存读取
                log.info("[HomeService] 锁已释放，尝试从缓存读取{}，用户ID: {}，重试次数: {}", dataType, userId, i + 1);
                Object cached = redisUtil.getValue(cacheKey);
                if (cached != null) {
                    T result = convertCachedData(cached, clazz, dataType, userId, cacheKey);
                    if (result != null) {
                        log.info("[HomeService] 从缓存获取用户 {} 的{}（重试成功）", userId, dataType);
                        return result;
                    }
                }
            }
        }

        // 重试失败，直接调用API
        log.warn("[HomeService] 等待重试失败，直接从API获取用户 {} 的{}", userId, dataType);
        return apiSupplier.get();
    }

    @Override
    public void preloadUserCache(String userId, Double latitude, Double longitude, String cityName, String ip) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[HomeService] 用户ID为空，跳过缓存预加载");
            return;
        }

        log.info("[HomeService] 开始预加载用户 {} 的天气和每日一签缓存", userId);

        try {
            getWeather(userId, latitude, longitude, cityName, ip);
        } catch (Exception e) {
            log.error("[HomeService] 预加载用户 {} 的天气缓存失败: {}", userId, e.getMessage(), e);
        }

        try {
            getDailyQuote(userId);
        } catch (Exception e) {
            log.error("[HomeService] 预加载用户 {} 的每日一签缓存失败: {}", userId, e.getMessage(), e);
        }

        log.info("[HomeService] 用户 {} 的天气和每日一签缓存预加载完成", userId);
    }

    @Override
    public WeatherApi.WeatherInfo refreshWeather(String userId, Double latitude, Double longitude, String cityName, String ip) {
        log.info("[HomeService] 强制刷新用户 {} 的天气信息", userId);
        
        WeatherApi.WeatherInfo weatherInfo = getWeatherFromApi(latitude, longitude, cityName, ip);
        
        if (userId != null && !userId.isEmpty() && weatherInfo != null) {
            String today = getTodayDate();
            String cacheKey = buildWeatherCacheKey(userId, today);
            long expireSeconds = getSecondsUntilNextDay();
            redisUtil.setKey(cacheKey, weatherInfo, expireSeconds, TimeUnit.SECONDS);
            log.info("[HomeService] 用户 {} 的天气信息已强制刷新并更新缓存", userId);
        }
        
        return weatherInfo;
    }

    @Override
    public DailyQuoteApi.DailyQuote refreshDailyQuote(String userId) {
        log.info("[HomeService] 强制刷新用户 {} 的每日一签", userId);
        
        DailyQuoteApi.DailyQuote dailyQuote = dailyQuoteApi.getDailyQuote();
        
        if (userId != null && !userId.isEmpty() && dailyQuote != null) {
            String today = getTodayDate();
            String cacheKey = buildDailyQuoteCacheKey(userId, today);
            long expireSeconds = getSecondsUntilNextDay();
            redisUtil.setKey(cacheKey, dailyQuote, expireSeconds, TimeUnit.SECONDS);
            log.info("[HomeService] 用户 {} 的每日一签已强制刷新并更新缓存", userId);
        }
        
        return dailyQuote;
    }

    /**
     * 从API获取天气信息
     */
    private WeatherApi.WeatherInfo getWeatherFromApi(Double latitude, Double longitude, String cityName, String ip) {
        // 优先使用IP地址获取位置和天气信息（使用天地图API）
        if (ip != null && !ip.trim().isEmpty()) {
            return weatherApi.getWeatherInfoByIp(ip, latitude, longitude, cityName);
        }
        
        // 如果没有IP，使用原有的经纬度方式
        if (latitude == null || longitude == null) {
            latitude = 34.7466;
            longitude = 113.6254;
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = "郑州";
            }
        }
        return weatherApi.getWeatherInfoByCoordinates(latitude, longitude, cityName);
    }
}
