package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 附加费用视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetAdditionalFeeVO {
    /**
     * 附加费用ID
     */
    private String id;

    /**
     * 费用名称
     */
    private String name;

    /**
     * 费用金额
     */
    private BigDecimal amount;

    /**
     * 费用日期（yyyy-MM-dd）
     */
    private String feeDate;

    /**
     * 备注
     */
    private String remark;
}

