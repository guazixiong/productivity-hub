package com.pbad.bookmark.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 网址批量更新DTO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkUrlBatchUpdateDTO {
    /**
     * 网址ID列表
     */
    private List<String> urlIds;

    /**
     * 要添加的标签ID列表
     */
    private List<String> addTagIds;

    /**
     * 要移除的标签ID列表
     */
    private List<String> removeTagIds;
}

