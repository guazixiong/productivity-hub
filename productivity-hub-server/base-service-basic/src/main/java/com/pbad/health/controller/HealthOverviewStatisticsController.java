package com.pbad.health.controller;

import com.pbad.health.domain.vo.HealthCalendarVO;
import com.pbad.health.domain.vo.HealthOverviewVO;
import com.pbad.health.service.HealthOverviewStatisticsService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 综合统计Controller.
 * 关联需求：REQ-HEALTH-004
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/statistics")
@RequiredArgsConstructor
public class HealthOverviewStatisticsController {

    private final HealthOverviewStatisticsService overviewStatisticsService;

    /**
     * 查询健康数据概览.
     * 关联接口：API-REQ-HEALTH-004-01
     *
     * @param period 统计周期（today/week/month），默认today
     * @return 健康数据概览
     */
    @GetMapping("/overview")
    public ApiResponse<HealthOverviewVO> getOverview(@RequestParam(required = false) String period) {
        String userId = RequestUserContext.getUserId();
        HealthOverviewVO overview = overviewStatisticsService.getOverview(period, userId);
        return ApiResponse.ok(overview);
    }

    /**
     * 查询健康数据日历.
     * 关联接口：API-REQ-HEALTH-004-02
     *
     * @param year  年份，默认当前年份
     * @param month 月份（1-12），默认当前月份
     * @return 健康数据日历
     */
    @GetMapping("/calendar")
    public ApiResponse<HealthCalendarVO> getCalendar(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        String userId = RequestUserContext.getUserId();
        HealthCalendarVO calendar = overviewStatisticsService.getCalendar(year, month, userId);
        return ApiResponse.ok(calendar);
    }
}

