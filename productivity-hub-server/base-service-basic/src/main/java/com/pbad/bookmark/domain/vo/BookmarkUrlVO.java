package com.pbad.bookmark.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 网址视图对象（VO）.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkUrlVO {
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
     * 标签列表
     */
    private List<BookmarkTagVO> tags;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;
}

