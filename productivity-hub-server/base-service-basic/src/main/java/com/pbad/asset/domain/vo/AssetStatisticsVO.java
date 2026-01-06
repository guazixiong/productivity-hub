package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产统计汇总视图对象（VO）.
 *
 * <p>用于 {@link com.pbad.asset.service.AssetStatisticsService#getStatistics()} 接口的返回结果，</p>
 * <p>提供资产数量与金额的整体概览数据。</p>
 */
@Data
public class AssetStatisticsVO {

    /**
     * 资产总数
     */
    private Integer totalAssets;

    /**
     * 正在服役资产数量
     */
    private Integer inServiceAssets;

    /**
     * 已退役资产数量
     */
    private Integer retiredAssets;

    /**
     * 资产总价值（购入价 + 附加费用）
     */
    private BigDecimal totalValue;
}


