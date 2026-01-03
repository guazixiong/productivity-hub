package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计概览视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class StatisticsOverviewVO {
    /**
     * 购入金额
     */
    private BigDecimal purchaseAmount;

    /**
     * 购入件数
     */
    private Integer purchaseCount;

    /**
     * 日均总额
     */
    private BigDecimal totalDailyAverage;
}

