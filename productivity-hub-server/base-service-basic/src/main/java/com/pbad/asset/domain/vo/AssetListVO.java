package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产列表视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetListVO {
    /**
     * 资产ID
     */
    private String id;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产价格
     */
    private BigDecimal price;

    /**
     * 资产图片URL
     */
    private String image;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 购买日期（yyyy-MM-dd）
     */
    private String purchaseDate;

    /**
     * 资产总价值
     */
    private BigDecimal totalValue;

    /**
     * 日均价格
     */
    private BigDecimal dailyAveragePrice;

    /**
     * 附加费用合计
     */
    private BigDecimal additionalFeesTotal;
}

