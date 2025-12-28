package com.pbad.image.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传DTO.
 * 关联需求：REQ-IMG-001
 * 关联接口：API-REQ-IMG-001-01
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ImageUploadDTO {

    /**
     * 图片文件
     */
    private MultipartFile file;

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
     * 图片描述
     */
    private String description;
}

