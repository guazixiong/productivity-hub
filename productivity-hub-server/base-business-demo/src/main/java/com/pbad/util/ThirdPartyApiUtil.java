package com.pbad.util;

import cn.hutool.http.HttpRequest;
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
     * Open-Meteo 天气API
     */
    private static final String OPEN_METEO_API_URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia/Shanghai";

    /**
     * 每日一言API
     */
    private static final String DAILY_QUOTE_API_URL = "https://v1.hitokoto.cn/";

    /**
     * 逆地理编码API（用于根据经纬度获取位置信息）
     */
    private static final String REVERSE_GEOCODE_API_URL_TEMPLATE = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=%.4f&longitude=%.4f&localityLanguage=zh";

    /**
     * 郑州经纬度（默认位置）
     */
    private static final double ZHENGZHOU_LATITUDE = 34.7466;
    private static final double ZHENGZHOU_LONGITUDE = 113.6254;

    /**
     * 根据经纬度获取位置信息（逆地理编码）.
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 位置信息（城市、省份、地址），如果获取失败返回默认值
     */
    public static LocationInfo getLocationInfoByCoordinates(double latitude, double longitude) {
        try {
            String url = String.format(REVERSE_GEOCODE_API_URL_TEMPLATE, latitude, longitude);
            // 使用HttpRequest显式设置请求头，避免gzip压缩问题
            String body = HttpRequest.get(url)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")  // 禁用压缩，避免ZipException
                    .execute()
                    .body();
            JSONObject obj = JSON.parseObject(body);
            if (obj != null) {
                String city = firstNonBlank(
                    obj.getString("city"),
                    obj.getString("locality"),
                    obj.getString("principalSubdivision")
                );
                String province = firstNonBlank(
                    obj.getString("principalSubdivision"),
                    obj.getString("countryName")
                );
                
                // 尝试从localityInfo中获取详细地址
                String address = null;
                try {
                    JSONObject localityInfo = obj.getJSONObject("localityInfo");
                    if (localityInfo != null) {
                        com.alibaba.fastjson.JSONArray administrative = localityInfo.getJSONArray("administrative");
                        if (administrative != null && administrative.size() > 0) {
                            JSONObject adminObj = administrative.getJSONObject(0);
                            if (adminObj != null) {
                                address = adminObj.getString("name");
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
                
                if (isBlank(address)) {
                    address = firstNonBlank(city, province, obj.getString("countryName"));
                }
                
                if (!isBlank(city) || !isBlank(province)) {
                    return new LocationInfo(
                        isBlank(city) ? "未知" : city,
                        isBlank(province) ? "未知" : province,
                        isBlank(address) ? (isBlank(city) ? "未知" : city) : address
                    );
                }
            }
        } catch (Exception e) {
            log.warn("获取位置信息失败: {}", e.getMessage());
        }
        // 返回默认值（郑州）
        return new LocationInfo("郑州", "河南省", "郑州市");
    }

    /**
     * 获取指定经纬度的天气信息（使用 Open-Meteo API）.
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param cityName  城市名称（用于显示，如果为空则通过逆地理编码获取）
     * @return 天气信息，如果获取失败返回默认值
     */
    public static WeatherInfo getWeatherInfoByCoordinates(double latitude, double longitude, String cityName) {
        // 如果未提供城市名称，通过逆地理编码获取
        LocationInfo locationInfo = null;
        if (isBlank(cityName)) {
            locationInfo = getLocationInfoByCoordinates(latitude, longitude);
            cityName = locationInfo.getCity();
        }
        
        try {
            String url = String.format(OPEN_METEO_API_URL_TEMPLATE, latitude, longitude);
            // 使用HttpRequest显式设置请求头，避免gzip压缩问题
            String body = HttpRequest.get(url)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")  // 禁用压缩，避免ZipException
                    .execute()
                    .body();
            JSONObject obj = JSON.parseObject(body);
            if (obj != null) {
                JSONObject current = obj.getJSONObject("current");
                if (current != null) {
                    // 温度（摄氏度）
                    Double temp = current.getDouble("temperature_2m");
                    String tempStr = temp != null ? String.format("%.0f", temp) : "未知";
                    
                    // 天气代码转换为中文描述
                    Integer weatherCode = current.getInteger("weather_code");
                    String weather = convertWeatherCode(weatherCode);
                    
                    // 湿度
                    Double humidity = current.getDouble("relative_humidity_2m");
                    String humidityStr = humidity != null ? String.format("%.0f%%", humidity) : "未知";
                    
                    // 风速
                    Double windSpeed = current.getDouble("wind_speed_10m");
                    String windStr = windSpeed != null ? String.format("%.1f km/h", windSpeed) : "未知";
                    
                    // 如果有位置信息，使用位置信息；否则使用提供的城市名称
                    if (locationInfo != null) {
                        return new WeatherInfo(
                            locationInfo.getCity(),
                            locationInfo.getProvince(),
                            locationInfo.getAddress(),
                            weather,
                            tempStr,
                            windStr,
                            humidityStr
                        );
                    } else {
                        return new WeatherInfo(cityName, null, null, weather, tempStr, windStr, humidityStr);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取{}天气信息失败: {}", cityName, e.getMessage());
        }
        // 返回默认值
        if (locationInfo != null) {
            // 使用位置信息，确保province不为null
            return new WeatherInfo(
                locationInfo.getCity(),
                locationInfo.getProvince() != null ? locationInfo.getProvince() : "未知",
                locationInfo.getAddress(),
                "未知", "未知", "未知", "未知"
            );
        }
        // 如果没有位置信息，尝试获取默认位置信息
        LocationInfo defaultLocation = getLocationInfoByCoordinates(latitude, longitude);
        return new WeatherInfo(
            defaultLocation.getCity(),
            defaultLocation.getProvince() != null ? defaultLocation.getProvince() : "未知",
            defaultLocation.getAddress(),
            "未知", "未知", "未知", "未知"
        );
    }

    /**
     * 获取指定城市的天气信息（兼容旧方法，默认使用郑州经纬度）.
     *
     * @param city 城市名称，如：郑州、北京、上海等
     * @return 天气信息，如果获取失败返回默认值
     */
    public static WeatherInfo getWeatherInfo(String city) {
        if (isBlank(city)) {
            city = "郑州";
        }
        // 默认使用郑州经纬度
        return getWeatherInfoByCoordinates(ZHENGZHOU_LATITUDE, ZHENGZHOU_LONGITUDE, city);
    }

    /**
     * 获取郑州的天气信息（用于钉钉推送）.
     *
     * @return 天气信息
     */
    public static WeatherInfo getZhengzhouWeatherInfo() {
        return getWeatherInfoByCoordinates(ZHENGZHOU_LATITUDE, ZHENGZHOU_LONGITUDE, "郑州");
    }

    /**
     * 将 Open-Meteo 天气代码转换为中文描述.
     * 参考：https://www.open-meteo.com/en/docs#weathercodes
     */
    private static String convertWeatherCode(Integer code) {
        if (code == null) {
            return "未知";
        }
        switch (code) {
            case 0:
                return "晴朗";
            case 1:
            case 2:
            case 3:
                return "多云";
            case 45:
            case 48:
                return "雾";
            case 51:
            case 53:
            case 55:
                return "小雨";
            case 56:
            case 57:
                return "冻雨";
            case 61:
            case 63:
            case 65:
                return "雨";
            case 66:
            case 67:
                return "冻雨";
            case 71:
            case 73:
            case 75:
                return "雪";
            case 77:
                return "雪粒";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 85:
            case 86:
                return "阵雪";
            case 95:
                return "雷暴";
            case 96:
            case 99:
                return "雷暴伴冰雹";
            default:
                return "未知";
        }
    }

    /**
     * 获取每日一言.
     *
     * @return 每日一言信息，如果获取失败返回默认值
     */
    public static DailyQuote getDailyQuote() {
        try {
            // 使用HttpRequest显式设置请求头，避免gzip压缩问题
            String body = HttpRequest.get(DAILY_QUOTE_API_URL)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")  // 禁用压缩，避免ZipException
                    .execute()
                    .body();
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
     * 位置信息数据类.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocationInfo {
        private String city;
        private String province;
        private String address;
    }

    /**
     * 天气信息数据类.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeatherInfo {
        private String city;
        private String province;
        private String address;
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

