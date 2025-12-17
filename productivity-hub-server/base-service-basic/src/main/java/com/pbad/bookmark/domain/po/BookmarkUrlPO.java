package com.pbad.bookmark.domain.po;

import lombok.Data;

/**
 * 网址实体类（PO）.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkUrlPO {
    /**
     * 网址ID
     */
    private String id;

    /**
     * 网址标题
     */
    private String title;

    /**
     * 跳转URL
     */
    private String url;

    /**
     * 网站图标URL
     */
    private String iconUrl;

    /**
     * 网址描述
     */
    private String description;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}

