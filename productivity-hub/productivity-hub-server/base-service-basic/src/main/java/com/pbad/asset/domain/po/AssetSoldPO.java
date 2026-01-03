package com.pbad.asset.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 资产卖出实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetSoldPO {
    /**
     * 卖出记录ID
     */
    private String id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 卖出价格
     */
    private BigDecimal soldPrice;

    /**
     * 卖出日期
     */
    private Date soldDate;

    /**
     * 卖出原因
     */
    private String soldReason;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

