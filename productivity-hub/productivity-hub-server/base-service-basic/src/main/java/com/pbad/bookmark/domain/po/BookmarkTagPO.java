package com.pbad.bookmark.domain.po;

import lombok.Data;

/**
 * 标签实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkTagPO {
    /**
     * 标签ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 父标签ID（一级标签为NULL，二级标签指向一级标签）
     */
    private String parentId;

    /**
     * 标签层级（1=一级标签，2=二级标签）
     */
    private Integer level;

    /**
     * 排序顺序（数字越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}

