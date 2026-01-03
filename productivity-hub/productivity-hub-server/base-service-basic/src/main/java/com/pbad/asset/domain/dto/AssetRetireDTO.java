package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 资产退役DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetRetireDTO {
    /**
     * 退役日期（yyyy-MM-dd）
     */
    @NotBlank(message = "退役日期不能为空")
    private String retiredDate;
}

