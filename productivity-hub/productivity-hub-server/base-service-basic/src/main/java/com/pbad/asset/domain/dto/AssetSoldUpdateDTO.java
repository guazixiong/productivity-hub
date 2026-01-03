package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 资产卖出更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetSoldUpdateDTO {
    /**
     * 卖出记录ID
     */
    @NotBlank(message = "卖出记录ID不能为空")
    private String id;

    /**
     * 卖出价格（>0）
     */
    @NotNull(message = "卖出价格不能为空")
    @DecimalMin(value = "0.01", message = "卖出价格必须大于0")
    private BigDecimal soldPrice;

    /**
     * 卖出日期（yyyy-MM-dd）
     */
    @NotBlank(message = "卖出日期不能为空")
    private String soldDate;

    /**
     * 卖出原因（0-200字符）
     */
    @Size(max = 200, message = "卖出原因长度不能超过200个字符")
    private String soldReason;

    /**
     * 备注（0-500字符）
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

