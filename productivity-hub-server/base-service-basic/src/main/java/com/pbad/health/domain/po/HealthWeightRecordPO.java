package com.pbad.health.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 体重记录持久化对象（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class HealthWeightRecordPO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID（用于用户数据隔离）
     */
    private String userId;

    /**
     * 记录日期
     */
    private Date recordDate;

    /**
     * 记录时间（精确到分钟）
     */
    private Date recordTime;

    /**
     * 体重（单位：公斤，范围：20.00-300.00）
     */
    private BigDecimal weightKg;

    /**
     * 体脂率（单位：%，范围：5.00-50.00）
     */
    private BigDecimal bodyFatPercentage;

    /**
     * 肌肉量（单位：公斤）
     */
    private BigDecimal muscleMassKg;

    /**
     * 身高（单位：厘米，范围：100.00-250.00）
     */
    private BigDecimal heightCm;

    /**
     * BMI值（身体质量指数，自动计算）
     */
    private BigDecimal bmi;

    /**
     * 健康状态（偏瘦、正常、偏胖、肥胖，根据BMI自动判断）
     */
    private String healthStatus;

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

