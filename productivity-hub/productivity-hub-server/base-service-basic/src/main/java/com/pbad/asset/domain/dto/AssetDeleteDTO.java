package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 资产删除DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetDeleteDTO {
    /**
     * 操作类型，固定值"delete"
     */
    @NotBlank(message = "操作类型不能为空")
    private String action;

    /**
     * 版本号（乐观锁）
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}

