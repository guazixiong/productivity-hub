package com.pbad.bookmark.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * Excel 导入预览结果.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkImportPreviewVO {

    /**
     * 解析到的总行数（不含表头）
     */
    private Integer total;

    /**
     * 预览数据列表
     */
    private List<BookmarkImportPreviewItemVO> items;
}


