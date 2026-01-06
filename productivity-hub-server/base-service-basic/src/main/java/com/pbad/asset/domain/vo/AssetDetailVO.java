package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产详情视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetDetailVO {
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
     * 备注
     */
    private String remark;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 购买日期（yyyy-MM-dd）
     */
    private String purchaseDate;

    /**
     * 是否启用保修
     */
    private Boolean warrantyEnabled;

    /**
     * 保修截止日期（yyyy-MM-dd）
     */
    private String warrantyEndDate;

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
     * 使用日期（yyyy-MM-dd）
     */
    private String usageDate;

    /**
     * 是否正在服役
     */
    private Boolean inService;

    /**
     * 退役日期（yyyy-MM-dd）
     */
    private String retiredDate;

    /**
     * 资产状态
     */
    private String status;

    /**
     * 资产总价值
     */
    private BigDecimal totalValue;

    /**
     * 使用天数
     */
    private Integer usageDays;

    /**
     * 日均价格
     */
    private BigDecimal dailyAveragePrice;

    /**
     * 附加费用列表
     */
    private List<AssetAdditionalFeeVO> additionalFees;

    /**
     * 卖出信息
     */
    private AssetSoldVO soldInfo;
}

