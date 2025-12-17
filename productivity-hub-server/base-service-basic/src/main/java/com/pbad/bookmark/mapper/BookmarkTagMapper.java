package com.pbad.bookmark.mapper;

import com.pbad.bookmark.domain.po.BookmarkTagPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签 Mapper 接口.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkTagMapper {

    /**
     * 查询所有标签
     *
     * @return 标签列表
     */
    List<BookmarkTagPO> selectAll();

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签
     */
    BookmarkTagPO selectById(@Param("id") String id);

    /**
     * 根据父标签ID查询子标签列表
     *
     * @param parentId 父标签ID
     * @return 子标签列表
     */
    List<BookmarkTagPO> selectByParentId(@Param("parentId") String parentId);

    /**
     * 根据层级查询标签列表
     *
     * @param level 层级（1=一级标签，2=二级标签）
     * @return 标签列表
     */
    List<BookmarkTagPO> selectByLevel(@Param("level") Integer level);

    /**
     * 根据名称和父标签ID查询标签（用于检查重复）
     *
     * @param name     标签名称
     * @param parentId 父标签ID
     * @return 标签
     */
    BookmarkTagPO selectByNameAndParentId(@Param("name") String name, @Param("parentId") String parentId);

    /**
     * 插入标签
     *
     * @param tag 标签
     * @return 插入行数
     */
    int insertTag(BookmarkTagPO tag);

    /**
     * 更新标签
     *
     * @param tag 标签
     * @return 更新行数
     */
    int updateTag(BookmarkTagPO tag);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除行数
     */
    int deleteTag(@Param("id") String id);

    /**
     * 批量更新标签排序顺序
     *
     * @param tags 标签列表（包含id和sortOrder）
     * @return 更新行数
     */
    int batchUpdateSortOrder(@Param("tags") List<BookmarkTagPO> tags);

    /**
     * 统计标签下的网址数量
     *
     * @param tagId 标签ID
     * @return 网址数量
     */
    int countUrlsByTagId(@Param("tagId") String tagId);

    /**
     * 统计一级标签（包括其所有子标签）下的网址数量（去重）
     *
     * @param parentTagId 一级标签ID
     * @return 网址数量
     */
    int countUrlsByParentTagId(@Param("parentTagId") String parentTagId);
}

