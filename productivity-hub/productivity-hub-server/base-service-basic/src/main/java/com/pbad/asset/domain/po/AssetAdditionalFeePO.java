package com.pbad.asset.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 附加费用实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetAdditionalFeePO {
    /**
     * 附加费用ID
     */
    private String id;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 费用名称
     */
    private String name;

    /**
     * 费用金额
     */
    private BigDecimal amount;

    /**
     * 费用日期
     */
    private Date feeDate;

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

