package com.pbad.asset.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 资产分类创建DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetCategoryCreateDTO {
    /**
     * 分类名称（1-50字符）
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 50, message = "分类名称长度必须在1-50个字符之间")
    private String name;

    /**
     * 分类图标
     */
    @Size(max = 50, message = "分类图标长度不能超过50个字符")
    private String icon;

    /**
     * 父分类ID（为空表示大分类，有值表示小分类）
     */
    private String parentId;
}

