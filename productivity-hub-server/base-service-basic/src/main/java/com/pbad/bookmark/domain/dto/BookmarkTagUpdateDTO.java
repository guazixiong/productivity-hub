package com.pbad.bookmark.domain.dto;

import lombok.Data;

/**
 * 标签更新DTO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkTagUpdateDTO {
    /**
     * 标签ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 排序顺序（数字越小越靠前）
     */
    private Integer sortOrder;
}

