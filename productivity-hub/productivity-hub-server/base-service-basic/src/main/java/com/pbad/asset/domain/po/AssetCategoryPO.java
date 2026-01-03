package com.pbad.asset.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 资产分类实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetCategoryPO {
    /**
     * 分类ID
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 父分类ID（为空表示大分类，有值表示小分类）
     */
    private String parentId;

    /**
     * 分类级别（1表示大分类，2表示小分类）
     */
    private Integer level;

    /**
     * 是否默认分类
     */
    private Boolean isDefault;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

