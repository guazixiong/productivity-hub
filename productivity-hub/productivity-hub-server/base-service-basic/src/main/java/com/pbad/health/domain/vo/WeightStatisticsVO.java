package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 体重统计数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightStatisticsVO {
    /**
     * 最新体重（公斤）
     */
    private BigDecimal latestWeight;

    /**
     * 目标体重（公斤）
     */
    private BigDecimal targetWeight;

    /**
     * 与目标体重的差距（公斤）
     */
    private BigDecimal gapFromTarget;

    /**
     * 平均体重（公斤）
     */
    private BigDecimal averageWeight;

    /**
     * 最高体重（公斤）
     */
    private BigDecimal maxWeight;

    /**
     * 最低体重（公斤）
     */
    private BigDecimal minWeight;

    /**
     * 体重变化（公斤，相对于周期开始时的体重）
     */
    private BigDecimal weightChange;

    /**
     * 体重变化率（%，相对于周期开始时的体重）
     */
    private BigDecimal weightChangeRate;

    /**
     * 记录数
     */
    private Integer recordCount;
}

