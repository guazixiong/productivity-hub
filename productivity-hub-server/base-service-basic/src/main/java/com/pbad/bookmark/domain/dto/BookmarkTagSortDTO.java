package com.pbad.bookmark.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 标签排序DTO.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class BookmarkTagSortDTO {
    /**
     * 标签ID列表（按排序后的顺序）
     */
    private List<String> tagIds;
}

