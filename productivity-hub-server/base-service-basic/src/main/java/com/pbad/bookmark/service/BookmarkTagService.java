package com.pbad.bookmark.service;

import com.pbad.bookmark.domain.dto.BookmarkTagCreateDTO;
import com.pbad.bookmark.domain.dto.BookmarkTagSortDTO;
import com.pbad.bookmark.domain.dto.BookmarkTagUpdateDTO;
import com.pbad.bookmark.domain.vo.BookmarkTagVO;

import java.util.List;

/**
 * 标签服务接口.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkTagService {

    /**
     * 获取所有标签（树形结构）
     *
     * @return 标签树
     */
    List<BookmarkTagVO> getTagTree();

    /**
     * 获取一级标签列表
     *
     * @return 一级标签列表
     */
    List<BookmarkTagVO> getParentTags();

    /**
     * 根据父标签ID获取子标签列表
     *
     * @param parentId 父标签ID
     * @return 子标签列表
     */
    List<BookmarkTagVO> getChildTags(String parentId);

    /**
     * 创建标签
     *
     * @param createDTO 创建DTO
     * @return 创建的标签
     */
    BookmarkTagVO createTag(BookmarkTagCreateDTO createDTO);

    /**
     * 更新标签
     *
     * @param updateDTO 更新DTO
     * @return 更新后的标签
     */
    BookmarkTagVO updateTag(BookmarkTagUpdateDTO updateDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(String id);

    /**
     * 更新标签排序
     *
     * @param sortDTO 排序DTO
     */
    void updateTagSort(BookmarkTagSortDTO sortDTO);
}

