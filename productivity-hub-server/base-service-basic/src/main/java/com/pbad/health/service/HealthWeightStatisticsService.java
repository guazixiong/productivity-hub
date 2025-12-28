package com.pbad.health.service;

import com.pbad.health.domain.dto.WeightStatisticsQueryDTO;
import com.pbad.health.domain.dto.WeightTrendQueryDTO;
import com.pbad.health.domain.vo.WeightStatisticsVO;
import com.pbad.health.domain.vo.WeightTrendVO;

/**
 * 体重数据统计服务接口.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWeightStatisticsService {

    /**
     * 查询体重统计数据.
     * 关联接口：API-REQ-HEALTH-002-01
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 统计数据
     */
    WeightStatisticsVO getStatistics(WeightStatisticsQueryDTO queryDTO, String userId);

    /**
     * 查询体重趋势数据.
     * 关联接口：API-REQ-HEALTH-002-02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 趋势数据
     */
    WeightTrendVO getTrend(WeightTrendQueryDTO queryDTO, String userId);
}

