package com.pbad.health.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 运动记录更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseRecordUpdateDTO {
    /**
     * 运动类型（可选，枚举值：跑步、游泳、骑行、力量训练、瑜伽、有氧运动、球类运动、其他）
     */
    private String exerciseType;

    /**
     * 运动日期（可选，格式：yyyy-MM-dd）
     */
    private String exerciseDate;

    /**
     * 运动时长（可选，分钟，范围：1-1440）
     */
    private Integer durationMinutes;

    /**
     * 消耗卡路里（可选，单位：千卡）
     */
    private Integer caloriesBurned;

    /**
     * 运动距离（可选，单位：公里，适用于跑步、骑行等）
     */
    private java.math.BigDecimal distanceKm;

    /**
     * 平均心率（可选，单位：次/分钟）
     */
    private Integer heartRateAvg;

    /**
     * 最大心率（可选，单位：次/分钟）
     */
    private Integer heartRateMax;

    /**
     * 训练计划ID（可选）
     */
    private String trainingPlanId;

    /**
     * 训练动作参考链接（可选，JSON数组）
     */
    private List<String> exerciseActionRefUrl;

    /**
     * 备注信息（可选）
     */
    private String notes;
}

