package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 训练计划更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class TrainingPlanUpdateDTO {
    /**
     * 计划名称（可选，1-128字符）
     */
    private String planName;

    /**
     * 计划类型（可选，枚举值：减脂、增肌、塑形、耐力提升、康复训练、其他）
     */
    private String planType;

    /**
     * 目标持续天数（可选）
     */
    private Integer targetDurationDays;

    /**
     * 每日目标卡路里（可选）
     */
    private Integer targetCaloriesPerDay;

    /**
     * 计划描述（可选）
     */
    private String description;

    /**
     * 开始日期（可选，格式：yyyy-MM-dd）
     */
    private String startDate;

    /**
     * 结束日期（可选，格式：yyyy-MM-dd）
     */
    private String endDate;
}

