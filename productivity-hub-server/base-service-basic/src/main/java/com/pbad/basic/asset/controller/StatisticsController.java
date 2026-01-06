package com.pbad.basic.asset.controller;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.vo.AssetCardStatisticsVO;
import com.pbad.asset.domain.vo.AssetDistributionVO;
import com.pbad.asset.domain.vo.CategoryStatisticsVO;
import com.pbad.asset.domain.vo.StatisticsOverviewVO;
import com.pbad.asset.domain.vo.StatisticsTrendVO;
import com.pbad.asset.service.AssetStatisticsService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计分析控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final AssetStatisticsService statisticsService;

    /**
     * 获取统计概览
     * 接口编号：API-REQ-003-01
     */
    @GetMapping("/overview")
    public ApiResponse<StatisticsOverviewVO> getOverview(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        StatisticsOverviewVO overview = statisticsService.getOverview(query);
        return ApiResponse.ok(overview);
    }

    /**
     * 获取日均趋势图表数据
     * 接口编号：API-REQ-003-03
     */
    @GetMapping("/daily-average-trend")
    public ApiResponse<StatisticsTrendVO> getDailyAverageTrend(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        StatisticsTrendVO trend = statisticsService.getDailyAverageTrend(query);
        return ApiResponse.ok(trend);
    }

    /**
     * 获取资产总额趋势图表数据
     * 接口编号：API-REQ-003-04
     */
    @GetMapping("/total-value-trend")
    public ApiResponse<StatisticsTrendVO> getTotalValueTrend(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        StatisticsTrendVO trend = statisticsService.getTotalValueTrend(query);
        return ApiResponse.ok(trend);
    }

    /**
     * 获取资产分布图表数据
     * 接口编号：API-REQ-003-05
     */
    @GetMapping("/asset-distribution")
    public ApiResponse<List<AssetDistributionVO>> getAssetDistribution(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        List<AssetDistributionVO> distribution = statisticsService.getAssetDistribution(query);
        return ApiResponse.ok(distribution);
    }

    /**
     * 获取分类统计图表数据
     * 接口编号：API-REQ-003-06
     */
    @GetMapping("/category-statistics")
    public ApiResponse<List<CategoryStatisticsVO>> getCategoryStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        List<CategoryStatisticsVO> categoryStatistics = statisticsService.getCategoryStatistics(query);
        return ApiResponse.ok(categoryStatistics);
    }

    /**
     * 获取资产卡片统计信息
     */
    @GetMapping("/asset-card")
    public ApiResponse<AssetCardStatisticsVO> getAssetCardStatistics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        StatisticsQueryDTO query = new StatisticsQueryDTO();
        query.setPeriod(period);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        AssetCardStatisticsVO statistics = statisticsService.getAssetCardStatistics(query);
        return ApiResponse.ok(statistics);
    }

}

