package com.pbad.health.service;

import com.pbad.health.domain.dto.WaterStatisticsQueryDTO;
import com.pbad.health.domain.dto.WaterTrendQueryDTO;
import com.pbad.health.domain.vo.WaterStatisticsVO;
import com.pbad.health.domain.vo.WaterTrendVO;

/**
 * 饮水数据统计服务接口.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWaterStatisticsService {

    /**
     * 查询饮水统计数据.
     * 关联接口：API-REQ-HEALTH-002-01
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 统计数据
     */
    WaterStatisticsVO getStatistics(WaterStatisticsQueryDTO queryDTO, String userId);

    /**
     * 查询饮水趋势数据.
     * 关联接口：API-REQ-HEALTH-002-02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 趋势数据
     */
    WaterTrendVO getTrend(WaterTrendQueryDTO queryDTO, String userId);
}

