package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 体重趋势数据项VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightTrendDataVO {
    /**
     * 日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 体重（公斤）
     */
    private BigDecimal weightKg;

    /**
     * BMI值
     */
    private BigDecimal bmi;

    /**
     * 体脂率（%）
     */
    private BigDecimal bodyFatPercentage;

    /**
     * 与前一天相比的变化（公斤）
     */
    private BigDecimal changeFromPrevious;
}

