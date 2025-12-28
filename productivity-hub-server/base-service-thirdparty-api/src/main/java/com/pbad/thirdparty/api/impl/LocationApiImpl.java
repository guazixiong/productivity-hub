package com.pbad.thirdparty.api.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.thirdparty.api.LocationApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 位置API实现类（使用天地图API）.
 *
 * @author pbad
 */
@Slf4j
@Service
public class LocationApiImpl implements LocationApi {

    /**
     * 天地图API Key.
     * 从配置文件读取：tianditu.api.key
     */
    @Value("${tianditu.api.key:40070907c045e3716ba06236e87531b6}")
    private String tiandituApiKey;
    
    // 天地图IP定位API
    private static final String TIANDITU_IP_LOCATION_URL = "http://api.tianditu.gov.cn/ipLocation";
    
    // 天地图逆地理编码API（根据经纬度获取地址）
    private static final String TIANDITU_REVERSE_GEOCODE_URL = "http://api.tianditu.gov.cn/geocoder";

    @Override
    public LocationInfo getLocationByIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            log.warn("IP地址为空，返回默认位置信息");
            return getDefaultLocation();
        }

        try {
            // 调用天地图IP定位API
            String url = String.format("%s?ip=%s&tk=%s", TIANDITU_IP_LOCATION_URL, ip, tiandituApiKey);
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .execute();
            
            // 检查HTTP状态码
            int statusCode = response.getStatus();
            if (statusCode != 200) {
                log.warn("天地图IP定位API返回非200状态码: {}, URL: {}", statusCode, url);
                return getDefaultLocation();
            }
            
            String body = response.body();
            
            // 检查响应内容是否为HTML（错误页面通常是HTML）
            if (body != null && (body.trim().startsWith("<!") || body.trim().startsWith("<html"))) {
                log.warn("天地图IP定位API返回HTML错误页面，可能是404或其他错误，URL: {}", url);
                return getDefaultLocation();
            }
            
            // 检查Content-Type是否为JSON
            String contentType = response.header("Content-Type");
            if (contentType != null && !contentType.toLowerCase().contains("json")) {
                log.warn("天地图IP定位API返回非JSON内容类型: {}, URL: {}", contentType, url);
                return getDefaultLocation();
            }
            
            JSONObject obj = JSON.parseObject(body);
            if (obj != null && obj.getInteger("status") != null && obj.getInteger("status") == 0) {
                JSONObject location = obj.getJSONObject("location");
                if (location != null) {
                    Double latitude = location.getDouble("lat");
                    Double longitude = location.getDouble("lon");
                    
                    // 如果获取到经纬度，使用逆地理编码获取详细地址
                    if (latitude != null && longitude != null) {
                        return getLocationByCoordinates(latitude, longitude);
                    }
                }
                
                // 如果返回了地址信息，直接解析
                JSONObject addressComponent = obj.getJSONObject("addressComponent");
                if (addressComponent != null) {
                    LocationInfo info = new LocationInfo();
                    info.setProvince(addressComponent.getString("province"));
                    info.setCity(addressComponent.getString("city"));
                    info.setDistrict(addressComponent.getString("district"));
                    
                    // 构建完整地址
                    StringBuilder addressBuilder = new StringBuilder();
                    if (info.getProvince() != null) {
                        addressBuilder.append(info.getProvince());
                    }
                    if (info.getCity() != null) {
                        addressBuilder.append(info.getCity());
                    }
                    if (info.getDistrict() != null) {
                        addressBuilder.append(info.getDistrict());
                    }
                    info.setAddress(addressBuilder.length() > 0 ? addressBuilder.toString() : "未知");
                    
                    if (location != null) {
                        info.setLatitude(location.getDouble("lat"));
                        info.setLongitude(location.getDouble("lon"));
                    }
                    
                    return info;
                }
            } else {
                log.warn("天地图IP定位API返回错误: {}", obj != null ? obj.getString("msg") : "未知错误");
            }
        } catch (Exception e) {
            log.error("调用天地图IP定位API失败: {}", e.getMessage(), e);
        }

        return getDefaultLocation();
    }

    @Override
    public LocationInfo getLocationByCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            log.warn("经纬度为空，返回默认位置信息");
            return getDefaultLocation();
        }

        try {
            // 调用天地图逆地理编码API
            String url = String.format("%s?ds={\"lon\":%.6f,\"lat\":%.6f}&tk=%s", 
                    TIANDITU_REVERSE_GEOCODE_URL, longitude, latitude, tiandituApiKey);
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .header("Accept", "application/json")
                    .header("Accept-Encoding", "identity")
                    .execute();
            
            // 检查HTTP状态码
            int statusCode = response.getStatus();
            if (statusCode != 200) {
                log.warn("天地图逆地理编码API返回非200状态码: {}, URL: {}", statusCode, url);
                return getDefaultLocation();
            }
            
            String body = response.body();
            
            // 检查响应内容是否为HTML（错误页面通常是HTML）
            if (body != null && (body.trim().startsWith("<!") || body.trim().startsWith("<html"))) {
                log.warn("天地图逆地理编码API返回HTML错误页面，可能是404或其他错误，URL: {}", url);
                return getDefaultLocation();
            }
            
            // 检查Content-Type是否为JSON
            String contentType = response.header("Content-Type");
            if (contentType != null && !contentType.toLowerCase().contains("json")) {
                log.warn("天地图逆地理编码API返回非JSON内容类型: {}, URL: {}", contentType, url);
                return getDefaultLocation();
            }
            
            JSONObject obj = JSON.parseObject(body);
            if (obj != null && obj.getInteger("status") != null && obj.getInteger("status") == 0) {
                JSONObject location = obj.getJSONObject("location");
                if (location != null) {
                    LocationInfo info = new LocationInfo();
                    info.setLatitude(latitude);
                    info.setLongitude(longitude);
                    
                    // 解析地址组件
                    JSONObject addressComponent = location.getJSONObject("addressComponent");
                    if (addressComponent != null) {
                        info.setProvince(addressComponent.getString("province"));
                        info.setCity(addressComponent.getString("city"));
                        info.setDistrict(addressComponent.getString("district"));
                        
                        // 构建完整地址
                        StringBuilder addressBuilder = new StringBuilder();
                        if (info.getProvince() != null) {
                            addressBuilder.append(info.getProvince());
                        }
                        if (info.getCity() != null) {
                            addressBuilder.append(info.getCity());
                        }
                        if (info.getDistrict() != null) {
                            addressBuilder.append(info.getDistrict());
                        }
                        info.setAddress(addressBuilder.length() > 0 ? addressBuilder.toString() : "未知");
                    } else {
                        // 如果没有地址组件，使用formatted_address
                        String formattedAddress = location.getString("formatted_address");
                        if (formattedAddress != null && !formattedAddress.trim().isEmpty()) {
                            info.setAddress(formattedAddress);
                            // 尝试从地址中提取省市信息
                            parseAddressFromString(formattedAddress, info);
                        } else {
                            info.setAddress("未知");
                        }
                    }
                    
                    return info;
                }
            } else {
                log.warn("天地图逆地理编码API返回错误: {}", obj != null ? obj.getString("msg") : "未知错误");
            }
        } catch (Exception e) {
            log.error("调用天地图逆地理编码API失败: {}", e.getMessage(), e);
        }

        return getDefaultLocation();
    }

    /**
     * 从地址字符串中解析省市信息
     */
    private void parseAddressFromString(String address, LocationInfo info) {
        if (address == null || address.trim().isEmpty()) {
            return;
        }
        
        // 简单的地址解析逻辑
        // 格式通常是：省 市 区/县 ...
        String[] parts = address.split("省|市|区|县");
        if (parts.length > 0) {
            info.setProvince(parts[0] + "省");
        }
        if (parts.length > 1) {
            info.setCity(parts[1] + "市");
        }
        if (parts.length > 2) {
            info.setDistrict(parts[2] + (address.contains("区") ? "区" : "县"));
        }
    }

    /**
     * 返回默认位置信息（郑州）
     */
    private LocationInfo getDefaultLocation() {
        LocationInfo info = new LocationInfo();
        info.setProvince("河南省");
        info.setCity("郑州");
        info.setDistrict("未知");
        info.setAddress("郑州市");
        info.setLatitude(34.7466);
        info.setLongitude(113.6254);
        return info;
    }
}

