package com.pbad.bookmark.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 网址子分组视图对象（二级标签分组）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkSubGroupVO {
    /**
     * 二级标签信息
     */
    private BookmarkTagVO tag;

    /**
     * 该标签下的网址列表
     */
    private List<BookmarkUrlVO> urls;
}

