package com.pbad.health.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 运动记录持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthExerciseRecordPO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID（用于用户数据隔离）
     */
    private String userId;

    /**
     * 运动类型（跑步、游泳、骑行、力量训练、瑜伽、有氧运动、球类运动、其他）
     */
    private String exerciseType;

    /**
     * 运动日期
     */
    private Date exerciseDate;

    /**
     * 运动时长（分钟，范围：1-1440）
     */
    private Integer durationMinutes;

    /**
     * 消耗卡路里（单位：千卡）
     */
    private Integer caloriesBurned;

    /**
     * 运动距离（单位：公里，适用于跑步、骑行等）
     */
    private BigDecimal distanceKm;

    /**
     * 平均心率（单位：次/分钟）
     */
    private Integer heartRateAvg;

    /**
     * 最大心率（单位：次/分钟）
     */
    private Integer heartRateMax;

    /**
     * 关联的训练计划ID（外键）
     */
    private String trainingPlanId;

    /**
     * 训练动作参考链接（支持多个链接，JSON格式存储）
     */
    private String exerciseActionRefUrl;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

