package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类统计视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CategoryStatisticsVO {
    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 资产数量
     */
    private Integer count;

    /**
     * 资产总价值
     */
    private BigDecimal totalValue;
}

