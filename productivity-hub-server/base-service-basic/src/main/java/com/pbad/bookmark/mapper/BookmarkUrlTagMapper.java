package com.pbad.bookmark.mapper;

import com.pbad.bookmark.domain.po.BookmarkUrlTagPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签网址关联 Mapper 接口.
 *
 * @author: system
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkUrlTagMapper {

    /**
     * 根据网址ID查询关联的标签ID列表
     *
     * @param urlId 网址ID
     * @return 标签ID列表
     */
    List<String> selectTagIdsByUrlId(@Param("urlId") String urlId);

    /**
     * 根据标签ID查询关联的网址ID列表
     *
     * @param tagId 标签ID
     * @return 网址ID列表
     */
    List<String> selectUrlIdsByTagId(@Param("tagId") String tagId);

    /**
     * 插入关联关系
     *
     * @param urlTag 关联关系
     * @return 插入行数
     */
    int insertUrlTag(BookmarkUrlTagPO urlTag);

    /**
     * 删除关联关系
     *
     * @param urlId 网址ID
     * @param tagId 标签ID
     * @return 删除行数
     */
    int deleteUrlTag(@Param("urlId") String urlId, @Param("tagId") String tagId);

    /**
     * 根据网址ID删除所有关联关系
     *
     * @param urlId 网址ID
     * @return 删除行数
     */
    int deleteByUrlId(@Param("urlId") String urlId);

    /**
     * 根据标签ID删除所有关联关系
     *
     * @param tagId 标签ID
     * @return 删除行数
     */
    int deleteByTagId(@Param("tagId") String tagId);

    /**
     * 批量插入关联关系
     *
     * @param urlTags 关联关系列表
     * @return 插入行数
     */
    int batchInsertUrlTags(@Param("urlTags") List<BookmarkUrlTagPO> urlTags);

    /**
     * 批量删除关联关系
     *
     * @param urlId 网址ID
     * @param tagIds 标签ID列表
     * @return 删除行数
     */
    int batchDeleteUrlTags(@Param("urlId") String urlId, @Param("tagIds") List<String> tagIds);
}

