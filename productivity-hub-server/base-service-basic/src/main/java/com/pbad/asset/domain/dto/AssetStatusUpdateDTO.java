package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 资产状态更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetStatusUpdateDTO {
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
     * 状态（IN_SERVICE、RETIRED、SOLD）
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}

