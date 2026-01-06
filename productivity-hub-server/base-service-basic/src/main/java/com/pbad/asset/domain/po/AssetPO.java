package com.pbad.asset.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 资产实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetPO {
    /**
     * 资产ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分类ID
     */
    private String categoryId;

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
     * 备注
     */
    private String remark;

    /**
     * 购买日期
     */
    private Date purchaseDate;

    /**
     * 是否启用保修
     */
    private Boolean warrantyEnabled;

    /**
     * 保修截止日期
     */
    private Date warrantyEndDate;

    /**
     * 是否按使用次数计算贬值
     */
    private Boolean depreciationByUsageCount;

    /**
     * 预计使用次数
     */
    private Integer expectedUsageCount;

    /**
     * 是否按使用日期计算贬值
     */
    private Boolean depreciationByUsageDate;

    /**
     * 使用日期
     */
    private Date usageDate;

    /**
     * 是否正在服役
     */
    private Boolean inService;

    /**
     * 退役日期
     */
    private Date retiredDate;

    /**
     * 资产状态：IN_SERVICE-正在服役，RETIRED-已退役，SOLD-已卖出
     */
    private String status;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 是否删除（软删除）
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

