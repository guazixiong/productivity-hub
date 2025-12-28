package com.pbad.health.domain.vo;

import lombok.Data;

/**
 * 按训练计划统计数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExercisePlanStatisticsVO {
    /**
     * 计划ID
     */
    private String planId;

    /**
     * 计划名称
     */
    private String planName;

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
}

