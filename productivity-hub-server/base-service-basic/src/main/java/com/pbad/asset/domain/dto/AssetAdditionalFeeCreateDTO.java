package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 附加费用创建DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetAdditionalFeeCreateDTO {
    /**
     * 资产ID
     */
    @NotBlank(message = "资产ID不能为空")
    private String assetId;

    /**
     * 费用名称（1-50字符）
     */
    @NotBlank(message = "费用名称不能为空")
    @Size(min = 1, max = 50, message = "费用名称长度必须在1-50个字符之间")
    private String name;

    /**
     * 费用金额（>0）
     */
    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额必须大于0")
    private BigDecimal amount;

    /**
     * 费用日期（yyyy-MM-dd）
     */
    @NotBlank(message = "费用日期不能为空")
    private String feeDate;

    /**
     * 备注（0-200字符）
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}

