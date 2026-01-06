package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产分布图表视图对象（VO）.
 *
 * <p>用于资产分布饼图的数据结构：</p>
 * <ul>
 *     <li>分类名称</li>
 *     <li>该分类对应的总金额</li>
 *     <li>金额占总体的百分比</li>
 * </ul>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetDistributionVO {

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 该分类的资产总价值
     */
    private BigDecimal value;

    /**
     * 占总资产价值的百分比（0-100）
     */
    private BigDecimal percentage;
}

