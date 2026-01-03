package com.pbad.health.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 体重记录更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightRecordUpdateDTO {
    /**
     * 记录日期（可选，格式：yyyy-MM-dd）
     */
    private String recordDate;

    /**
     * 记录时间（可选，格式：HH:mm）
     */
    private String recordTime;

    /**
     * 体重（可选，单位：公斤，范围：20.00-300.00）
     */
    private BigDecimal weightKg;

    /**
     * 体脂率（可选，单位：%，范围：5.00-50.00）
     */
    private BigDecimal bodyFatPercentage;

    /**
     * 肌肉量（可选，单位：公斤）
     */
    private BigDecimal muscleMassKg;

    /**
     * 身高（可选，单位：厘米，范围：100.00-250.00）
     */
    private BigDecimal heightCm;

    /**
     * 备注信息（可选）
     */
    private String notes;
}

