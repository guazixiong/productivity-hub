package com.pbad.image.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 批量图片上传DTO.
 * 关联需求：REQ-IMG-001
 * 关联接口：API-REQ-IMG-001-02
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ImageBatchUploadDTO {

    /**
     * 图片文件数组（最多10张）
     */
    private MultipartFile[] files;

    /**
     * 图片分类：avatar、bookmark、todo、health、article、other
     */
    private String category;

    /**
     * 业务模块标识
     */
    private String businessModule;

    /**
     * 业务关联ID
     */
    private String businessId;

    /**
     * 图片描述（应用于所有图片）
     */
    private String description;
}

