package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产卡片统计信息视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetCardStatisticsVO {
    /**
     * 资产价格（总购入金额，过滤已退役资产）
     */
    private BigDecimal totalPrice;

    /**
     * 资产总数
     */
    private Integer totalCount;

    /**
     * 总资产附加费用
     */
    private BigDecimal totalAdditionalFees;

    /**
     * 资产总价值（资产价格之和 + 资产附加费用之和）
     */
    private BigDecimal totalValue;

    /**
     * 日均总额 = SUM((每件资产价值 + 额外费用) / 使用天数)
     */
    private BigDecimal dailyAverageTotal;
}

