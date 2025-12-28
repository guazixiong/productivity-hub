package com.pbad.image.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片信息持久化对象.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class ImagePO {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 用户ID，用于用户数据隔离
     */
    private String userId;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 存储文件名（UUID + 扩展名）
     */
    private String storedFilename;

    /**
     * 文件存储路径（相对路径）
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（单位：字节）
     */
    private Long fileSize;

    /**
     * 文件类型（如：image/jpeg, image/png）
     */
    private String fileType;

    /**
     * 文件扩展名（如：jpg, png, gif）
     */
    private String fileExtension;

    /**
     * 图片宽度（单位：像素）
     */
    private Integer width;

    /**
     * 图片高度（单位：像素）
     */
    private Integer height;

    /**
     * 图片分类（avatar、bookmark、todo、health、article、other）
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
     * 缩略图存储路径（相对路径）
     */
    private String thumbnailPath;

    /**
     * 缩略图访问URL
     */
    private String thumbnailUrl;

    /**
     * 分享令牌（用于图片分享功能）
     */
    private String shareToken;

    /**
     * 分享链接过期时间（为空表示永久有效）
     */
    private LocalDateTime shareExpiresAt;

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 状态（ACTIVE、DELETED、ARCHIVED）
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

