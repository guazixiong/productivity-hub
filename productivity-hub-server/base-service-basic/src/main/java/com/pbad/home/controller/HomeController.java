package com.pbad.home.controller;

import com.pbad.home.service.HomeService;
import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.thirdparty.api.DailyQuoteApi;
import com.pbad.thirdparty.api.HotDataApi;
import com.pbad.thirdparty.api.LocationApi;
import com.pbad.thirdparty.api.WeatherApi;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页控制器.
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final DailyTechDigestTask dailyTechDigestTask;
    private final HotDataApi hotDataApi;
    private final HomeService homeService;
    private final LocationApi locationApi;

    @GetMapping("/hot-sections/names")
    public ApiResponse<List<String>> getHotSectionNames() {
        try {
            List<String> names = hotDataApi.getHotSectionNames();
            return ApiResponse.ok(names);
        } catch (Exception e) {
            log.error("获取热点标签列表失败", e);
            return ApiResponse.fail("获取热点标签列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot-sections/section")
    public ApiResponse<HotDataApi.HotSectionVO> getHotSection(
            @RequestParam String sectionName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取热点数据，sectionName: {}, limit: {}", sectionName, limit);
            HotDataApi.HotSectionVO section = hotDataApi.getHotSectionByName(sectionName, limit);
            log.info("返回热点数据，标签: {}, 数据条数: {}", sectionName, section.getItems().size());
            return ApiResponse.ok(section);
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return ApiResponse.fail("获取热点数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/hot-sections")
    public ApiResponse<List<HotDataApi.HotSectionVO>> getHotSections(
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            log.info("获取热点数据，limit参数: {}", limit);
            List<HotDataApi.HotSectionVO> sections = hotDataApi.getHotSections(limit);
            log.info("返回热点数据，板块数量: {}, 每个板块数据条数: {}",
                    sections.size(),
                    sections.isEmpty() ? 0 : sections.get(0).getItems().size());
            return ApiResponse.ok(sections);
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return ApiResponse.fail("获取热点数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/weather")
    public ApiResponse<WeatherApi.WeatherInfo> getWeather(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String ip) {
        try {
            // 获取当前用户ID（如果已登录）
            String userId = RequestUserContext.getUserId();
            
            // 使用HomeService获取天气信息（会从缓存或API获取）
            WeatherApi.WeatherInfo weatherInfo = homeService.getWeather(userId, latitude, longitude, cityName, ip);
            return ApiResponse.ok(weatherInfo);
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            return ApiResponse.fail("获取天气信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/daily-quote")
    public ApiResponse<DailyQuoteApi.DailyQuote> getDailyQuote() {
        try {
            // 获取当前用户ID（如果已登录）
            String userId = RequestUserContext.getUserId();
            
            // 使用HomeService获取每日一签（会从缓存或API获取）
            DailyQuoteApi.DailyQuote dailyQuote = homeService.getDailyQuote(userId);
            return ApiResponse.ok(dailyQuote);
        } catch (Exception e) {
            log.error("获取每日一签失败", e);
            return ApiResponse.fail("获取每日一签失败: " + e.getMessage());
        }
    }

    @GetMapping("/weather/refresh")
    public ApiResponse<WeatherApi.WeatherInfo> refreshWeather(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String ip) {
        try {
            // 获取当前用户ID（如果已登录）
            String userId = RequestUserContext.getUserId();
            
            // 强制刷新天气信息（调用第三方API并更新缓存）
            WeatherApi.WeatherInfo weatherInfo = homeService.refreshWeather(userId, latitude, longitude, cityName, ip);
            return ApiResponse.ok(weatherInfo);
        } catch (Exception e) {
            log.error("刷新天气信息失败", e);
            return ApiResponse.fail("刷新天气信息失败: " + e.getMessage());
        }
    }

    /**
     * 使用天地图API根据IP地址获取位置信息（新增接口，保留原有接口）.
     */
    @GetMapping("/location/tianditu")
    public ApiResponse<LocationApi.LocationInfo> getLocationByIp(
            @RequestParam(required = false) String ip) {
        try {
            if (ip == null || ip.trim().isEmpty()) {
                return ApiResponse.fail("IP地址不能为空");
            }
            
            LocationApi.LocationInfo locationInfo = locationApi.getLocationByIp(ip);
            return ApiResponse.ok(locationInfo);
        } catch (Exception e) {
            log.error("通过天地图API获取位置信息失败", e);
            return ApiResponse.fail("获取位置信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/daily-quote/refresh")
    public ApiResponse<DailyQuoteApi.DailyQuote> refreshDailyQuote() {
        try {
            // 获取当前用户ID（如果已登录）
            String userId = RequestUserContext.getUserId();
            
            // 强制刷新每日一签（调用第三方API并更新缓存）
            DailyQuoteApi.DailyQuote dailyQuote = homeService.refreshDailyQuote(userId);
            return ApiResponse.ok(dailyQuote);
        } catch (Exception e) {
            log.error("刷新每日一签失败", e);
            return ApiResponse.fail("刷新每日一签失败: " + e.getMessage());
        }
    }
}


