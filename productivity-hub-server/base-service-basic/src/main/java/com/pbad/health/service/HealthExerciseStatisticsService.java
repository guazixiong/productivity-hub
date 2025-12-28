package com.pbad.health.service;

import com.pbad.health.domain.dto.ExerciseStatisticsQueryDTO;
import com.pbad.health.domain.dto.ExerciseTrendQueryDTO;
import com.pbad.health.domain.vo.ExerciseStatisticsVO;
import com.pbad.health.domain.vo.ExerciseTrendVO;

/**
 * 运动数据统计服务接口.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthExerciseStatisticsService {

    /**
     * 查询运动统计数据.
     * 关联接口：API-REQ-HEALTH-002-01
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 统计数据
     */
    ExerciseStatisticsVO getStatistics(ExerciseStatisticsQueryDTO queryDTO, String userId);

    /**
     * 查询运动趋势数据.
     * 关联接口：API-REQ-HEALTH-002-02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 趋势数据
     */
    ExerciseTrendVO getTrend(ExerciseTrendQueryDTO queryDTO, String userId);
}

