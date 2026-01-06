package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 心愿单创建DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WishlistCreateDTO {
    /**
     * 心愿名称（1-100字符）
     */
    @NotBlank(message = "心愿名称不能为空")
    @Size(min = 1, max = 100, message = "心愿名称长度必须在1-100个字符之间")
    private String name;

    /**
     * 心愿价格（>0）
     */
    @NotNull(message = "心愿价格不能为空")
    @DecimalMin(value = "0.01", message = "心愿价格必须大于0")
    private BigDecimal price;

    /**
     * 心愿链接
     */
    @Size(max = 500, message = "心愿链接长度不能超过500个字符")
    private String link;

    /**
     * 备注（0-500字符）
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}

