package com.pbad.bookmark.mapper;

import com.pbad.bookmark.domain.po.BookmarkUrlPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 网址 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkUrlMapper {

    /**
     * 查询所有网址
     *
     * @param userId 用户ID
     * @return 网址列表
     */
    List<BookmarkUrlPO> selectAll(@Param("userId") String userId);

    /**
     * 根据ID查询网址
     *
     * @param id     网址ID
     * @param userId 用户ID
     * @return 网址
     */
    BookmarkUrlPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 根据标签ID查询网址列表
     *
     * @param tagId  标签ID
     * @param userId 用户ID
     * @return 网址列表
     */
    List<BookmarkUrlPO> selectByTagId(@Param("tagId") String tagId, @Param("userId") String userId);

    /**
     * 根据多个标签ID查询网址列表（任一标签匹配）
     *
     * @param tagIds 标签ID列表
     * @param userId 用户ID
     * @return 网址列表
     */
    List<BookmarkUrlPO> selectByTagIds(@Param("tagIds") List<String> tagIds, @Param("userId") String userId);

    /**
     * 根据关键词搜索网址（标题或描述）
     *
     * @param keyword 关键词
     * @param userId  用户ID
     * @return 网址列表
     */
    List<BookmarkUrlPO> searchByKeyword(@Param("keyword") String keyword, @Param("userId") String userId);

    /**
     * 插入网址
     *
     * @param url 网址
     * @return 插入行数
     */
    int insertUrl(BookmarkUrlPO url);

    /**
     * 更新网址
     *
     * @param url 网址
     * @return 更新行数
     */
    int updateUrl(BookmarkUrlPO url);

    /**
     * 删除网址
     *
     * @param id     网址ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteUrl(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量删除网址
     *
     * @param ids    网址ID列表
     * @param userId 用户ID
     * @return 删除行数
     */
    int batchDeleteUrls(@Param("ids") List<String> ids, @Param("userId") String userId);
}

