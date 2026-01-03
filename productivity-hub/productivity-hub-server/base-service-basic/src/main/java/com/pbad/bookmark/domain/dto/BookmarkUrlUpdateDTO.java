package com.pbad.bookmark.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 网址更新DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkUrlUpdateDTO {
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
     * 标签ID列表（支持多个标签）
     */
    private List<String> tagIds;
}

