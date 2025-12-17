package com.pbad.bookmark.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 标签视图对象（VO）.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkTagVO {
    /**
     * 标签ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 父标签ID
     */
    private String parentId;

    /**
     * 标签层级（1=一级标签，2=二级标签）
     */
    private Integer level;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 网址数量
     */
    private Integer urlCount;

    /**
     * 子标签列表（仅一级标签有）
     */
    private List<BookmarkTagVO> children;
}

