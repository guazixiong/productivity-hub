package com.pbad.health.controller;

import com.pbad.health.domain.dto.WeightStatisticsQueryDTO;
import com.pbad.health.domain.dto.WeightTrendQueryDTO;
import com.pbad.health.domain.vo.WeightStatisticsVO;
import com.pbad.health.domain.vo.WeightTrendVO;
import com.pbad.health.service.HealthWeightStatisticsService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 体重数据统计控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/weight")
@RequiredArgsConstructor
public class HealthWeightStatisticsController extends BaseHealthController {

    private final HealthWeightStatisticsService statisticsService;

    /**
     * 查询体重统计数据.
01
     *
     * @param queryDTO 查询参数
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<WeightStatisticsVO> getStatistics(WeightStatisticsQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            WeightStatisticsVO vo = statisticsService.getStatistics(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询体重统计数据失败", e);
            return ApiResponse.fail("查询体重统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 查询体重趋势数据.
02
     *
     * @param queryDTO 查询参数
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public ApiResponse<WeightTrendVO> getTrend(WeightTrendQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            WeightTrendVO vo = statisticsService.getTrend(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询体重趋势数据失败", e);
            return ApiResponse.fail("查询体重趋势数据失败：" + e.getMessage());
        }
    }
}

