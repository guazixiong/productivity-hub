package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 按运动类型统计数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseTypeStatisticsVO {
    /**
     * 运动类型
     */
    private String exerciseType;

    /**
     * 次数
     */
    private Integer count;

    /**
     * 总时长（分钟）
     */
    private Integer totalDuration;

    /**
     * 总卡路里
     */
    private Integer totalCalories;

    /**
     * 总距离（公里）
     */
    private BigDecimal totalDistance;
}

