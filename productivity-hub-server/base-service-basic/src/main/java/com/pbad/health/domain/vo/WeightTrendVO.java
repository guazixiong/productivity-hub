package com.pbad.health.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 体重趋势数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightTrendVO {
    /**
     * 天数
     */
    private Integer days;

    /**
     * 趋势数据列表
     */
    private List<WeightTrendDataVO> data;

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
}

