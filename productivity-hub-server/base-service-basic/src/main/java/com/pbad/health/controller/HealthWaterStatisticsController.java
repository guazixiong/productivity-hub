package com.pbad.health.controller;

import com.pbad.health.domain.dto.WaterStatisticsQueryDTO;
import com.pbad.health.domain.dto.WaterTrendQueryDTO;
import com.pbad.health.domain.vo.WaterStatisticsVO;
import com.pbad.health.domain.vo.WaterTrendVO;
import com.pbad.health.service.HealthWaterStatisticsService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 饮水数据统计控制器.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/water")
@RequiredArgsConstructor
public class HealthWaterStatisticsController extends BaseHealthController {

    private final HealthWaterStatisticsService statisticsService;

    /**
     * 查询饮水统计数据.
     * 关联接口：API-REQ-HEALTH-002-01
     *
     * @param queryDTO 查询参数
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<WaterStatisticsVO> getStatistics(WaterStatisticsQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            WaterStatisticsVO vo = statisticsService.getStatistics(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水统计数据失败", e);
            return ApiResponse.fail("查询饮水统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 查询饮水趋势数据.
     * 关联接口：API-REQ-HEALTH-002-02
     *
     * @param queryDTO 查询参数
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public ApiResponse<WaterTrendVO> getTrend(WaterTrendQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            WaterTrendVO vo = statisticsService.getTrend(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水趋势数据失败", e);
            return ApiResponse.fail("查询饮水趋势数据失败：" + e.getMessage());
        }
    }
}

