package com.pbad.thirdparty.api.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.LocationApi;
import com.pbad.thirdparty.api.WeatherApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 天气API实现类.
 *
 * @author pbad
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiImpl implements WeatherApi {

    private static final String OPEN_METEO_API_URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia/Shanghai";
    private static final String REVERSE_GEOCODE_API_URL_TEMPLATE = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=%.4f&longitude=%.4f&localityLanguage=zh";

    private final LocationApi locationApi;

    @Override
    public WeatherInfo getWeatherInfoByCoordinates(Double latitude, Double longitude, String cityName) {
        // 使用final变量，确保可以在lambda表达式中使用
        final Double finalLatitude;
        final Double finalLongitude;
        
        if (latitude == null || longitude == null) {
            finalLatitude = 34.7466;
            finalLongitude = 113.6254;
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = "郑州";
            }
        } else {
            finalLatitude = latitude;
            finalLongitude = longitude;
        }

        // 优化：如果提供了cityName，直接使用，避免调用反向地理编码API
        // 如果cityName为空，并行调用位置和天气API，减少总耗时
        LocationInfo locationInfo = null;
        CompletableFuture<LocationInfo> locationFuture = null;
        
        if (isBlank(cityName)) {
            // 并行获取位置信息（不阻塞天气API调用）
            locationFuture = CompletableFuture.supplyAsync(() -> 
                getLocationInfoByCoordinates(finalLatitude, finalLongitude));
        }

        // 立即调用天气API（不等待位置信息）
        JSONObject weatherData = null;
        try {
            String url = String.format(OPEN_METEO_API_URL_TEMPLATE, finalLatitude, finalLongitude);
            String body = HttpRequest.get(url)
                    .timeout(3000) // 减少超时时间：从5秒减少到3秒
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .execute()
                    .body();
            weatherData = JSON.parseObject(body);
        } catch (Exception e) {
            log.warn("获取{}天气信息失败: {}", cityName, e.getMessage());
        }

        // 如果正在获取位置信息，等待结果（最多等待2秒）
        if (locationFuture != null) {
            try {
                locationInfo = locationFuture.get(2, TimeUnit.SECONDS);
                if (locationInfo != null) {
                    cityName = locationInfo.getCity();
                }
            } catch (Exception e) {
                log.warn("获取位置信息超时或失败: {}", e.getMessage());
                // 位置信息获取失败，使用默认值
                locationInfo = new LocationInfo("郑州", "河南省", "郑州市");
            }
        }

        // 解析天气数据
        if (weatherData != null) {
            JSONObject current = weatherData.getJSONObject("current");
            if (current != null) {
                Double temp = current.getDouble("temperature_2m");
                String tempStr = temp != null ? String.format("%.0f", temp) : "未知";

                Integer weatherCode = current.getInteger("weather_code");
                String weather = convertWeatherCode(weatherCode);

                Double humidity = current.getDouble("relative_humidity_2m");
                String humidityStr = humidity != null ? String.format("%.0f%%", humidity) : "未知";

                Double windSpeed = current.getDouble("wind_speed_10m");
                String windStr = windSpeed != null ? String.format("%.1f km/h", windSpeed) : "未知";

                WeatherInfo info = new WeatherInfo();
                if (locationInfo != null) {
                    info.setCity(locationInfo.getCity());
                    info.setProvince(locationInfo.getProvince());
                    info.setAddress(locationInfo.getAddress());
                } else {
                    info.setCity(cityName);
                    info.setProvince("未知");
                    info.setAddress(cityName);
                }
                info.setWeather(weather);
                info.setTemp(tempStr);
                info.setWind(windStr);
                info.setHumidity(humidityStr);
                return info;
            }
        }

        // 天气API失败，返回位置信息（如果有）
        WeatherInfo info = new WeatherInfo();
        if (locationInfo != null) {
            info.setCity(locationInfo.getCity());
            info.setProvince(locationInfo.getProvince() != null ? locationInfo.getProvince() : "未知");
            info.setAddress(locationInfo.getAddress());
        } else {
            info.setCity(cityName);
            info.setProvince("未知");
            info.setAddress(cityName);
        }
        info.setWeather("未知");
        info.setTemp("未知");
        info.setWind("未知");
        info.setHumidity("未知");
        return info;
    }

    @Override
    public WeatherInfo getWeatherInfoByIp(String ip, Double latitude, Double longitude, String cityName) {
        // 优先使用IP地址通过天地图API获取位置信息
        LocationApi.LocationInfo locationInfo = null;
        if (ip != null && !ip.trim().isEmpty()) {
            try {
                locationInfo = locationApi.getLocationByIp(ip);
                if (locationInfo != null && locationInfo.getLatitude() != null && locationInfo.getLongitude() != null) {
                    latitude = locationInfo.getLatitude();
                    longitude = locationInfo.getLongitude();
                    if (cityName == null || cityName.trim().isEmpty()) {
                        cityName = locationInfo.getCity();
                    }
                }
            } catch (Exception e) {
                log.warn("通过IP获取位置信息失败: {}", e.getMessage());
            }
        }

        // 如果IP定位失败，使用传入的经纬度或默认值
        if (latitude == null || longitude == null) {
            latitude = 34.7466;
            longitude = 113.6254;
            if (cityName == null || cityName.trim().isEmpty()) {
                cityName = "郑州";
            }
        }

        // 如果还没有位置信息，尝试通过经纬度获取
        if (locationInfo == null) {
            try {
                locationInfo = locationApi.getLocationByCoordinates(latitude, longitude);
            } catch (Exception e) {
                log.warn("通过经纬度获取位置信息失败: {}", e.getMessage());
            }
        }

        // 如果仍然没有位置信息，使用原有的方法
        if (locationInfo == null) {
            LocationInfo oldLocationInfo = getLocationInfoByCoordinates(latitude, longitude);
            if (oldLocationInfo != null) {
                // 转换为LocationApi.LocationInfo
                LocationApi.LocationInfo apiLocationInfo = new LocationApi.LocationInfo();
                apiLocationInfo.setCity(oldLocationInfo.getCity());
                apiLocationInfo.setProvince(oldLocationInfo.getProvince());
                apiLocationInfo.setAddress(oldLocationInfo.getAddress());
                locationInfo = apiLocationInfo;
                if (cityName == null || cityName.trim().isEmpty()) {
                    cityName = locationInfo.getCity();
                }
            }
        }

        // 获取天气信息
        JSONObject weatherData = null;
        try {
            String url = String.format(OPEN_METEO_API_URL_TEMPLATE, latitude, longitude);
            String body = HttpRequest.get(url)
                    .timeout(3000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .execute()
                    .body();
            weatherData = JSON.parseObject(body);
        } catch (Exception e) {
            log.warn("获取{}天气信息失败: {}", cityName, e.getMessage());
        }

        // 解析天气数据
        if (weatherData != null) {
            JSONObject current = weatherData.getJSONObject("current");
            if (current != null) {
                Double temp = current.getDouble("temperature_2m");
                String tempStr = temp != null ? String.format("%.0f", temp) : "未知";

                Integer weatherCode = current.getInteger("weather_code");
                String weather = convertWeatherCode(weatherCode);

                Double humidity = current.getDouble("relative_humidity_2m");
                String humidityStr = humidity != null ? String.format("%.0f%%", humidity) : "未知";

                Double windSpeed = current.getDouble("wind_speed_10m");
                String windStr = windSpeed != null ? String.format("%.1f km/h", windSpeed) : "未知";

                WeatherInfo info = new WeatherInfo();
                if (locationInfo != null) {
                    info.setCity(locationInfo.getCity());
                    info.setProvince(locationInfo.getProvince());
                    info.setAddress(locationInfo.getAddress());
                } else {
                    info.setCity(cityName != null ? cityName : "郑州");
                    info.setProvince("未知");
                    info.setAddress(cityName != null ? cityName : "郑州市");
                }
                info.setWeather(weather);
                info.setTemp(tempStr);
                info.setWind(windStr);
                info.setHumidity(humidityStr);
                return info;
            }
        }

        // 天气API失败，返回位置信息（如果有）
        WeatherInfo info = new WeatherInfo();
        if (locationInfo != null) {
            info.setCity(locationInfo.getCity());
            info.setProvince(locationInfo.getProvince() != null ? locationInfo.getProvince() : "未知");
            info.setAddress(locationInfo.getAddress());
        } else {
            info.setCity(cityName != null ? cityName : "郑州");
            info.setProvince("未知");
            info.setAddress(cityName != null ? cityName : "郑州市");
        }
        info.setWeather("未知");
        info.setTemp("未知");
        info.setWind("未知");
        info.setHumidity("未知");
        return info;
    }


    private LocationInfo getLocationInfoByCoordinates(double latitude, double longitude) {
        try {
            String url = String.format(REVERSE_GEOCODE_API_URL_TEMPLATE, latitude, longitude);
            String body = HttpRequest.get(url)
                    .timeout(3000) // 减少超时时间：从5秒减少到3秒
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
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
        return new LocationInfo("郑州", "河南省", "郑州市");
    }

    private String convertWeatherCode(Integer code) {
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

    private String firstNonBlank(String... values) {
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

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private static class LocationInfo {
        private String city;
        private String province;
        private String address;

        public LocationInfo(String city, String province, String address) {
            this.city = city;
            this.province = province;
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public String getProvince() {
            return province;
        }

        public String getAddress() {
            return address;
        }
    }
}

