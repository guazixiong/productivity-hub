package com.pbad.home.controller;

import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.schedule.domain.vo.HotSectionVO;
import com.pbad.util.ThirdPartyApiUtil;
import com.pbad.util.ThirdPartyApiUtil.WeatherInfo;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页控制器.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final DailyTechDigestTask dailyTechDigestTask;

    /**
     * 获取所有热点标签列表
     *
     * @return 热点标签名称列表
     */
    @GetMapping("/hot-sections/names")
    public ApiResponse<List<String>> getHotSectionNames() {
        try {
            List<String> names = dailyTechDigestTask.getHotSectionNames();
            return ApiResponse.ok(names);
        } catch (Exception e) {
            log.error("获取热点标签列表失败", e);
            return ApiResponse.fail("获取热点标签列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定标签的热点数据
     *
     * @param sectionName 标签名称
     * @param limit 数据条数
     * @return 热点板块数据
     */
    @GetMapping("/hot-sections/section")
    public ApiResponse<HotSectionVO> getHotSection(
            @RequestParam String sectionName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("获取热点数据，sectionName: {}, limit: {}", sectionName, limit);
            HotSectionVO section = dailyTechDigestTask.getHotSectionByName(sectionName, limit);
            log.info("返回热点数据，标签: {}, 数据条数: {}", sectionName, section.getItems().size());
            return ApiResponse.ok(section);
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return ApiResponse.fail("获取热点数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取热点数据（兼容旧接口）
     *
     * @return 热点板块列表
     */
    @GetMapping("/hot-sections")
    public ApiResponse<List<HotSectionVO>> getHotSections(
            @RequestParam(defaultValue = "5") Integer limit) {
        try {
            log.info("获取热点数据，limit参数: {}", limit);
            List<HotSectionVO> sections = dailyTechDigestTask.getHotSections(limit);
            log.info("返回热点数据，板块数量: {}, 每个板块数据条数: {}", 
                    sections.size(), 
                    sections.isEmpty() ? 0 : sections.get(0).getItems().size());
            return ApiResponse.ok(sections);
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return ApiResponse.fail("获取热点数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取天气信息
     *
     * @param latitude  纬度（可选，默认使用郑州）
     * @param longitude 经度（可选，默认使用郑州）
     * @param cityName  城市名称（可选，用于显示）
     * @return 天气信息
     */
    @GetMapping("/weather")
    public ApiResponse<WeatherInfo> getWeather(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String cityName) {
        try {
            // 如果未提供经纬度，使用郑州的默认经纬度
            if (latitude == null || longitude == null) {
                latitude = 34.7466;  // 郑州纬度
                longitude = 113.6254; // 郑州经度
                if (cityName == null || cityName.trim().isEmpty()) {
                    cityName = "郑州";
                }
            }
            WeatherInfo weatherInfo = ThirdPartyApiUtil.getWeatherInfoByCoordinates(latitude, longitude, cityName);
            return ApiResponse.ok(weatherInfo);
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            return ApiResponse.fail("获取天气信息失败: " + e.getMessage());
        }
    }
}

