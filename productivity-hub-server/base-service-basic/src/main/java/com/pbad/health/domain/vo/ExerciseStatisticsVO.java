package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运动统计数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseStatisticsVO {
    /**
     * 总时长（分钟）
     */
    private Integer totalDuration;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 总卡路里
     */
    private Integer totalCalories;

    /**
     * 总距离（公里）
     */
    private BigDecimal totalDistance;

    /**
     * 平均时长（分钟）
     */
    private Integer averageDuration;

    /**
     * 平均卡路里
     */
    private Integer averageCalories;

    /**
     * 按类型统计（groupBy=type时返回）
     */
    private List<ExerciseTypeStatisticsVO> typeStatistics;

    /**
     * 按计划统计（groupBy=plan时返回）
     */
    private List<ExercisePlanStatisticsVO> planStatistics;
}

