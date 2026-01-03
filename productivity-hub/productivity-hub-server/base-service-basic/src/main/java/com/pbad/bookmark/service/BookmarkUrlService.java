package com.pbad.bookmark.service;

import com.pbad.bookmark.domain.dto.BookmarkUrlBatchUpdateDTO;
import com.pbad.bookmark.domain.dto.BookmarkUrlCreateDTO;
import com.pbad.bookmark.domain.dto.BookmarkUrlUpdateDTO;
import com.pbad.bookmark.domain.vo.BookmarkGroupVO;
import com.pbad.bookmark.domain.vo.BookmarkUrlVO;

import java.util.List;

/**
 * 网址服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface BookmarkUrlService {

    /**
     * 获取所有网址（按标签分组）
     *
     * @return 分组后的网址列表
     */
    List<BookmarkGroupVO> getUrlGroups();

    /**
     * 根据标签ID获取网址列表
     *
     * @param tagId 标签ID
     * @return 网址列表
     */
    List<BookmarkUrlVO> getUrlsByTagId(String tagId);

    /**
     * 搜索网址
     *
     * @param keyword 关键词
     * @return 网址列表
     */
    List<BookmarkUrlVO> searchUrls(String keyword);

    /**
     * 根据ID获取网址详情
     *
     * @param id 网址ID
     * @return 网址详情
     */
    BookmarkUrlVO getUrlById(String id);

    /**
     * 创建网址
     *
     * @param createDTO 创建DTO
     * @return 创建的网址
     */
    BookmarkUrlVO createUrl(BookmarkUrlCreateDTO createDTO);

    /**
     * 更新网址
     *
     * @param updateDTO 更新DTO
     * @return 更新后的网址
     */
    BookmarkUrlVO updateUrl(BookmarkUrlUpdateDTO updateDTO);

    /**
     * 删除网址
     *
     * @param id 网址ID
     */
    void deleteUrl(String id);

    /**
     * 批量删除网址
     *
     * @param ids 网址ID列表
     */
    void batchDeleteUrls(List<String> ids);

    /**
     * 批量更新网址标签
     *
     * @param batchUpdateDTO 批量更新DTO
     */
    void batchUpdateUrlTags(BookmarkUrlBatchUpdateDTO batchUpdateDTO);

    /**
     * 重新加载我的书签缓存
     * <p>
     * 用于应用启动预热或数据变更后刷新缓存。
     */
    void refreshBookmarkCache();
}

