package com.pbad.home.controller;

import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.thirdparty.api.HotDataApi;
import com.pbad.thirdparty.api.WeatherApi;
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
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final DailyTechDigestTask dailyTechDigestTask;
    private final HotDataApi hotDataApi;
    private final WeatherApi weatherApi;

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
            @RequestParam(required = false) String cityName) {
        try {
            if (latitude == null || longitude == null) {
                latitude = 34.7466;
                longitude = 113.6254;
                if (cityName == null || cityName.trim().isEmpty()) {
                    cityName = "郑州";
                }
            }
            WeatherApi.WeatherInfo weatherInfo = weatherApi.getWeatherInfoByCoordinates(latitude, longitude, cityName);
            return ApiResponse.ok(weatherInfo);
        } catch (Exception e) {
            log.error("获取天气信息失败", e);
            return ApiResponse.fail("获取天气信息失败: " + e.getMessage());
        }
    }
}


