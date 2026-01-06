package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 资产更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetUpdateDTO {
    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String id;

    /**
     * 版本号（乐观锁）
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;

    /**
     * 资产名称（1-100字符）
     */
    @NotBlank(message = "资产名称不能为空")
    @Size(min = 1, max = 100, message = "资产名称长度必须在1-100个字符之间")
    private String name;

    /**
     * 资产价格（>0）
     */
    @NotNull(message = "资产价格不能为空")
    @DecimalMin(value = "0.01", message = "资产价格必须大于0")
    private BigDecimal price;

    /**
     * 资产图片URL
     */
    @Size(max = 500, message = "资产图片URL长度不能超过500个字符")
    private String image;

    /**
     * 备注（0-500字符）
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 分类ID
     */
    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    /**
     * 购买日期（yyyy-MM-dd）
     */
    @NotBlank(message = "购买日期不能为空")
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
     * 预计使用次数（>0）
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
}

