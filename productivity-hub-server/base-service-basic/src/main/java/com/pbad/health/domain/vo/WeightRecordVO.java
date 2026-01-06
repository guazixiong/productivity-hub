package com.pbad.health.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 体重记录视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightRecordVO {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 记录日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date recordDate;

    /**
     * 记录时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private Date recordTime;

    /**
     * 体重（公斤）
     */
    private BigDecimal weightKg;

    /**
     * 体脂率（%）
     */
    private BigDecimal bodyFatPercentage;

    /**
     * 肌肉量（公斤）
     */
    private BigDecimal muscleMassKg;

    /**
     * 身高（厘米）
     */
    private BigDecimal heightCm;

    /**
     * BMI值（身体质量指数）
     */
    private BigDecimal bmi;

    /**
     * 健康状态（偏瘦、正常、偏胖、肥胖）
     */
    private String healthStatus;

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

