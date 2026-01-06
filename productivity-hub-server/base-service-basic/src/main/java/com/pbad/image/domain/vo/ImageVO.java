package com.pbad.image.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片信息视图对象.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class ImageVO {

    /**
     * 图片ID
     */
    private String id;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 存储文件名
     */
    private String storedFilename;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 缩略图访问URL
     */
    private String thumbnailUrl;

    /**
     * 文件大小（单位：字节）
     */
    private Long fileSize;

    /**
     * 图片宽度（单位：像素）
     */
    private Integer width;

    /**
     * 图片高度（单位：像素）
     */
    private Integer height;

    /**
     * 图片分类
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

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

