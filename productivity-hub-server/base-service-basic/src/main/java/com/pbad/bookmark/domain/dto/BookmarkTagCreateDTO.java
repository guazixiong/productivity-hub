package com.pbad.bookmark.domain.dto;

import lombok.Data;

/**
 * 标签创建DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkTagCreateDTO {
    /**
     * 标签名称
     */
    private String name;

    /**
     * 父标签ID（一级标签为NULL，二级标签指向一级标签）
     */
    private String parentId;

    /**
     * 排序顺序（数字越小越靠前）
     */
    private Integer sortOrder;
}

