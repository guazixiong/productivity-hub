package com.pbad.bookmark.domain.vo;

import lombok.Data;

/**
 * Excel导入结果视图对象.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkImportResultVO {
    /**
     * 总记录数
     */
    private Integer total;

    /**
     * 成功数
     */
    private Integer success;

    /**
     * 失败数
     */
    private Integer failed;

    /**
     * 跳过数
     */
    private Integer skipped;

    /**
     * 错误信息列表
     */
    private java.util.List<String> errors;
}

