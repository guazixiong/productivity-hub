package com.pbad.bookmark.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 网址分组视图对象（按标签分组）.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkGroupVO {
    /**
     * 一级标签信息
     */
    private BookmarkTagVO parentTag;

    /**
     * 二级标签分组列表
     */
    private List<BookmarkSubGroupVO> subGroups;

    /**
     * 未分类的网址列表（属于一级标签但不属于任何二级标签）
     */
    private List<BookmarkUrlVO> uncategorizedUrls;
}

