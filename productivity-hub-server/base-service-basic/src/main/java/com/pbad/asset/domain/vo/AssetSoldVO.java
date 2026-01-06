package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产卖出视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetSoldVO {
    /**
     * 卖出记录ID
     */
    private String id;

    /**
     * 卖出价格
     */
    private BigDecimal soldPrice;

    /**
     * 卖出日期（yyyy-MM-dd）
     */
    private String soldDate;

    /**
     * 卖出原因
     */
    private String soldReason;

    /**
     * 备注
     */
    private String remark;
}

