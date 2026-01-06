package com.pbad.image.controller;

import com.pbad.image.domain.dto.ImageQueryDTO;
import com.pbad.image.domain.dto.ImageShareDTO;
import com.pbad.image.domain.dto.ImageUpdateDTO;
import com.pbad.image.domain.vo.ImageShareVO;
import com.pbad.image.domain.vo.ImageStatisticsVO;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.service.ImageAccessService;
import com.pbad.image.service.ImageService;
import com.pbad.image.service.ImageShareService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片管理控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageShareService imageShareService;
    private final ImageAccessService imageAccessService;

    // ==================== 图片上传 ====================

    /**
     * 上传单张图片
     * API-REQ-IMG-001-01
     */
    @PostMapping("/upload")
    public ApiResponse<ImageVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "businessModule", required = false) String businessModule,
            @RequestParam(value = "businessId", required = false) String businessId,
            @RequestParam(value = "description", required = false) String description) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageVO imageVO = imageService.uploadImage(file, category, businessModule, businessId, description, userId);
        return ApiResponse.ok("上传成功", imageVO);
    }

    /**
     * 批量上传图片
     * API-REQ-IMG-001-02
     */
    @PostMapping("/upload/batch")
    public ApiResponse<List<ImageVO>> batchUploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "businessModule", required = false) String businessModule,
            @RequestParam(value = "businessId", required = false) String businessId,
            @RequestParam(value = "description", required = false) String description) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        List<ImageVO> imageList = imageService.batchUploadImages(files, category, businessModule, businessId, description, userId);
        return ApiResponse.ok("批量上传成功", imageList);
    }

    // ==================== 图片查询 ====================

    /**
     * 分页查询图片列表
     * API-REQ-IMG-002-01
     */
    @GetMapping("/list")
    public ApiResponse<PageResult<ImageVO>> listImages(ImageQueryDTO queryDTO) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        PageResult<ImageVO> pageResult = imageService.listImages(queryDTO, userId);
        return ApiResponse.ok(pageResult);
    }

    /**
     * 查询图片详情
     * API-REQ-IMG-002-02
     */
    @GetMapping("/{id}")
    public ApiResponse<ImageVO> getImageById(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageVO imageVO = imageService.getImageById(id, userId);
        return ApiResponse.ok(imageVO);
    }

    // ==================== 图片更新 ====================

    /**
     * 更新图片信息
     * API-REQ-IMG-003-01
     */
    @PutMapping("/{id}")
    public ApiResponse<ImageVO> updateImage(@PathVariable("id") String id,
                                          @RequestBody ImageUpdateDTO updateDTO) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageVO imageVO = imageService.updateImage(id, updateDTO, userId);
        return ApiResponse.ok("更新成功", imageVO);
    }

    // ==================== 图片删除 ====================

    /**
     * 删除图片（软删除）
     * API-REQ-IMG-004-01
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        imageService.deleteImage(id, userId);
        return ApiResponse.ok("删除成功", null);
    }

    /**
     * 批量删除图片（软删除）
     * API-REQ-IMG-004-02
     */
    @DeleteMapping("/batch")
    public ApiResponse<Integer> batchDeleteImages(@RequestBody List<String> ids) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        int count = imageService.batchDeleteImages(ids, userId);
        return ApiResponse.ok("批量删除成功，共删除 " + count + " 张图片", count);
    }

    /**
     * 恢复图片
     * API-REQ-IMG-004-03
     */
    @PostMapping("/{id}/restore")
    public ApiResponse<ImageVO> restoreImage(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageVO imageVO = imageService.restoreImage(id, userId);
        return ApiResponse.ok("恢复成功", imageVO);
    }

    /**
     * 归档图片
     * API-REQ-IMG-004-04
     */
    @PostMapping("/{id}/archive")
    public ApiResponse<ImageVO> archiveImage(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageVO imageVO = imageService.archiveImage(id, userId);
        return ApiResponse.ok("归档成功", imageVO);
    }

    // ==================== 图片统计 ====================

    /**
     * 查询图片统计信息
     * API-REQ-IMG-005-01
     */
    @GetMapping("/statistics")
    public ApiResponse<ImageStatisticsVO> getStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);

        ImageStatisticsVO statistics = imageService.getStatistics(userId, start, end);
        return ApiResponse.ok(statistics);
    }

    // ==================== 图片分享 ====================

    /**
     * 创建分享链接
     * API-REQ-IMG-006-01
     */
    @PostMapping("/{id}/share")
    public ApiResponse<ImageShareVO> createShare(@PathVariable("id") String id,
                                                 @RequestBody(required = false) ImageShareDTO shareDTO) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        ImageShareVO shareVO = imageShareService.createShare(id, shareDTO, userId);
        return ApiResponse.ok("创建分享链接成功", shareVO);
    }

    /**
     * 取消分享
     * API-REQ-IMG-006-02
     */
    @DeleteMapping("/{id}/share")
    public ApiResponse<Void> cancelShare(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        imageShareService.cancelShare(id, userId);
        return ApiResponse.ok("取消分享成功", null);
    }

    /**
     * 根据分享令牌获取图片信息
     * API-REQ-IMG-006-03
     */
    @GetMapping("/share/{shareToken}")
    public ApiResponse<ImageShareVO> getShareInfo(@PathVariable("shareToken") String shareToken) {
        ImageShareVO shareVO = imageShareService.getShareInfo(shareToken);
        return ApiResponse.ok(shareVO);
    }

    // ==================== 图片访问 ====================

    /**
     * 通过ID访问图片（增加访问统计）
     * API-REQ-IMG-001-03
     */
    @GetMapping("/{id}/access")
    public ApiResponse<ImageVO> accessImageById(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        ImageVO imageVO = imageAccessService.accessImageById(id, userId);
        return ApiResponse.ok(imageVO);
    }

    /**
     * 通过分享令牌访问图片（增加访问统计）
     * API-REQ-IMG-006-04
     */
    @GetMapping("/share/{shareToken}/access")
    public ApiResponse<ImageVO> accessImageByShareToken(@PathVariable("shareToken") String shareToken) {
        ImageVO imageVO = imageAccessService.accessImageByShareToken(shareToken);
        return ApiResponse.ok(imageVO);
    }

    /**
     * 读取图片文件内容
     * API-REQ-IMG-001-04
     */
    @GetMapping("/file/{path:.*}")
    public ResponseEntity<byte[]> readImageFile(@PathVariable("path") String path) {
        byte[] fileBytes = imageAccessService.readImageFile(path);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // 根据实际文件类型设置
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }

    // ==================== 工具方法 ====================

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}

