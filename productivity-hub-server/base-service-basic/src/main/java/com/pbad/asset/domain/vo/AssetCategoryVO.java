package com.pbad.asset.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 资产分类视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetCategoryVO {
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
     * 子分类列表（仅大分类有此字段）
     */
    private List<AssetCategoryVO> children;

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

    /**
     * 资产数量
     */
    private Integer assetCount;
}

