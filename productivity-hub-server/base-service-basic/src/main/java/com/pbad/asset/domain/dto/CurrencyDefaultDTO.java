package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 设置默认货币DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CurrencyDefaultDTO {
    /**
     * 货币代码
     */
    @NotBlank(message = "货币代码不能为空")
    private String currencyCode;
}

