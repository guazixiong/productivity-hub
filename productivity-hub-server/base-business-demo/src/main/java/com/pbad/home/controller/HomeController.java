package com.pbad.home.controller;

import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.schedule.domain.vo.HotSectionVO;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 获取热点数据
     *
     * @return 热点板块列表
     */
    @GetMapping("/hot-sections")
    public ApiResponse<List<HotSectionVO>> getHotSections() {
        try {
            List<HotSectionVO> sections = dailyTechDigestTask.getHotSections();
            return ApiResponse.ok(sections);
        } catch (Exception e) {
            log.error("获取热点数据失败", e);
            return ApiResponse.fail("获取热点数据失败: " + e.getMessage());
        }
    }
}

