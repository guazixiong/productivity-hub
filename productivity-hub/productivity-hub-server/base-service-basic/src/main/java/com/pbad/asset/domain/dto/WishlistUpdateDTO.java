package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 心愿单更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WishlistUpdateDTO {
    /**
     * 心愿单ID
     */
    @NotBlank(message = "心愿单ID不能为空")
    private String id;

    /**
     * 操作类型，固定值"update"
     */
    @NotBlank(message = "操作类型不能为空")
    private String action;

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

    /**
     * 是否已达成
     */
    private Boolean achieved;

    /**
     * 实现时间（yyyy-MM-dd HH:mm:ss）
     */
    private String achievedAt;
}

