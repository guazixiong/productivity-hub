package com.pbad.health.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 运动记录视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseRecordVO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 运动类型
     */
    private String exerciseType;

    /**
     * 运动日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date exerciseDate;

    /**
     * 运动时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 消耗卡路里（千卡）
     */
    private Integer caloriesBurned;

    /**
     * 运动距离（公里）
     */
    private BigDecimal distanceKm;

    /**
     * 平均心率（次/分钟）
     */
    private Integer heartRateAvg;

    /**
     * 最大心率（次/分钟）
     */
    private Integer heartRateMax;

    /**
     * 关联的训练计划ID
     */
    private String trainingPlanId;

    /**
     * 训练计划名称（关联查询）
     */
    private String trainingPlanName;

    /**
     * 训练动作参考链接（JSON数组）
     */
    private List<String> exerciseActionRefUrl;

    /**
     * 备注信息
     */
    private String notes;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}

