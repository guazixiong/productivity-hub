package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新资产设置DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetSettingsUpdateDTO {
    /**
     * 退役资产不计入总额
     */
    @NotNull(message = "退役资产不计入总额设置不能为空")
    private Boolean excludeRetired;

    /**
     * 总资产算二手盈利
     */
    @NotNull(message = "总资产算二手盈利设置不能为空")
    private Boolean calculateProfit;
}

