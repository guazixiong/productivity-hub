package com.pbad.basic.bookmark.controller;

import com.pbad.bookmark.domain.dto.*;
import com.pbad.bookmark.domain.vo.*;
import com.pbad.bookmark.service.BookmarkImportService;
import com.pbad.bookmark.service.BookmarkTagService;
import com.pbad.bookmark.service.BookmarkUrlService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 宝藏类网址控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkTagService tagService;
    private final BookmarkUrlService urlService;
    private final BookmarkImportService importService;

    // ==================== 标签管理 ====================

    /**
     * 获取标签树
     */
    @GetMapping("/tags/tree")
    public ApiResponse<List<BookmarkTagVO>> getTagTree() {
        List<BookmarkTagVO> tags = tagService.getTagTree();
        return ApiResponse.ok(tags);
    }

    /**
     * 获取一级标签列表
     */
    @GetMapping("/tags/parent")
    public ApiResponse<List<BookmarkTagVO>> getParentTags() {
        List<BookmarkTagVO> tags = tagService.getParentTags();
        return ApiResponse.ok(tags);
    }

    /**
     * 根据父标签ID获取子标签列表
     */
    @GetMapping("/tags/child/{parentId}")
    public ApiResponse<List<BookmarkTagVO>> getChildTags(@PathVariable String parentId) {
        List<BookmarkTagVO> tags = tagService.getChildTags(parentId);
        return ApiResponse.ok(tags);
    }

    /**
     * 创建标签
     */
    @PostMapping("/tags")
    public ApiResponse<BookmarkTagVO> createTag(@RequestBody BookmarkTagCreateDTO createDTO) {
        BookmarkTagVO tag = tagService.createTag(createDTO);
        return ApiResponse.ok(tag);
    }

    /**
     * 更新标签
     */
    @PutMapping("/tags")
    public ApiResponse<BookmarkTagVO> updateTag(@RequestBody BookmarkTagUpdateDTO updateDTO) {
        BookmarkTagVO tag = tagService.updateTag(updateDTO);
        return ApiResponse.ok(tag);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ApiResponse.ok(null);
    }

    /**
     * 更新标签排序
     */
    @PutMapping("/tags/sort")
    public ApiResponse<Void> updateTagSort(@RequestBody BookmarkTagSortDTO sortDTO) {
        tagService.updateTagSort(sortDTO);
        return ApiResponse.ok(null);
    }

    // ==================== 网址管理 ====================

    /**
     * 获取所有网址（按标签分组）
     */
    @GetMapping("/urls/groups")
    public ApiResponse<List<BookmarkGroupVO>> getUrlGroups() {
        List<BookmarkGroupVO> groups = urlService.getUrlGroups();
        return ApiResponse.ok(groups);
    }

    /**
     * 根据标签ID获取网址列表
     */
    @GetMapping("/urls/tag/{tagId}")
    public ApiResponse<List<BookmarkUrlVO>> getUrlsByTagId(@PathVariable String tagId) {
        List<BookmarkUrlVO> urls = urlService.getUrlsByTagId(tagId);
        return ApiResponse.ok(urls);
    }

    /**
     * 搜索网址
     */
    @GetMapping("/urls/search")
    public ApiResponse<List<BookmarkUrlVO>> searchUrls(@RequestParam String keyword) {
        List<BookmarkUrlVO> urls = urlService.searchUrls(keyword);
        return ApiResponse.ok(urls);
    }

    /**
     * 根据ID获取网址详情
     */
    @GetMapping("/urls/{id}")
    public ApiResponse<BookmarkUrlVO> getUrlById(@PathVariable String id) {
        BookmarkUrlVO url = urlService.getUrlById(id);
        return ApiResponse.ok(url);
    }

    /**
     * 创建网址
     */
    @PostMapping("/urls")
    public ApiResponse<BookmarkUrlVO> createUrl(@RequestBody BookmarkUrlCreateDTO createDTO) {
        BookmarkUrlVO url = urlService.createUrl(createDTO);
        return ApiResponse.ok(url);
    }

    /**
     * 更新网址
     */
    @PutMapping("/urls")
    public ApiResponse<BookmarkUrlVO> updateUrl(@RequestBody BookmarkUrlUpdateDTO updateDTO) {
        BookmarkUrlVO url = urlService.updateUrl(updateDTO);
        return ApiResponse.ok(url);
    }

    /**
     * 删除网址
     */
    @DeleteMapping("/urls/{id}")
    public ApiResponse<Void> deleteUrl(@PathVariable String id) {
        urlService.deleteUrl(id);
        return ApiResponse.ok(null);
    }

    /**
     * 批量删除网址
     */
    @DeleteMapping("/urls/batch")
    public ApiResponse<Void> batchDeleteUrls(@RequestBody List<String> ids) {
        urlService.batchDeleteUrls(ids);
        return ApiResponse.ok(null);
    }

    /**
     * 批量更新网址标签
     */
    @PutMapping("/urls/batch/tags")
    public ApiResponse<Void> batchUpdateUrlTags(@RequestBody BookmarkUrlBatchUpdateDTO batchUpdateDTO) {
        urlService.batchUpdateUrlTags(batchUpdateDTO);
        return ApiResponse.ok(null);
    }

    // ==================== Excel导入导出 ====================

    /**
     * 导入Excel文件
     */
    @PostMapping("/import")
    public ApiResponse<BookmarkImportResultVO> importFromExcel(@RequestParam("file") MultipartFile file) {
        BookmarkImportResultVO result = importService.importFromExcel(file);
        return ApiResponse.ok(result);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = importService.downloadTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "bookmark_import_template.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(template);
    }
}

