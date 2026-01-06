package com.pbad.bookmark.domain.vo;

import lombok.Data;

/**
 * Excel 导入预览单行数据.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkImportPreviewItemVO {

    /**
     * 一级标签名称
     */
    private String parentTagName;

    /**
     * 二级标签名称
     */
    private String childTagName;

    /**
     * 标题
     */
    private String title;

    /**
     * URL
     */
    private String url;

    /**
     * 描述
     */
    private String description;

    /**
     * 图标 URL
     */
    private String iconUrl;
}


