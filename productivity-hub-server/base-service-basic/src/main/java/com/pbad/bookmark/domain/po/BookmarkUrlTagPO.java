package com.pbad.bookmark.domain.po;

import lombok.Data;

/**
 * 标签网址关联实体类（PO）.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkUrlTagPO {
    /**
     * 关联ID
     */
    private String id;

    /**
     * 网址ID
     */
    private String urlId;

    /**
     * 标签ID
     */
    private String tagId;

    /**
     * 创建时间
     */
    private String createdAt;
}

