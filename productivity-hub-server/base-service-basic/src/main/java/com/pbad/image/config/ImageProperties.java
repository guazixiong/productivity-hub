package com.pbad.image.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 图片管理配置属性.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {

    /**
     * 图片存储根目录（相对路径或绝对路径）
     */
    private String baseDir = "uploads/images";

    /**
     * 图片访问URL前缀
     */
    private String urlPrefix = "/uploads/images";

    /**
     * 最大文件大小（单位：字节，默认1MB）
     */
    private long maxFileSize = 1024 * 1024; // 1MB

    /**
     * 允许的文件类型（MIME类型）
     */
    private List<String> allowedTypes = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
    );

    /**
     * 允许的文件扩展名
     */
    private List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    /**
     * 缩略图宽度（像素）
     */
    private int thumbnailWidth = 200;

    /**
     * 缩略图高度（像素）
     */
    private int thumbnailHeight = 200;

    /**
     * 图片压缩质量（0.0-1.0，默认0.8）
     */
    private double compressQuality = 0.8;

    /**
     * 批量上传最大数量
     */
    private int batchMaxCount = 10;

    /**
     * 是否启用访问统计
     */
    private boolean enableAccessStatistics = true;

    /**
     * 原图缓存天数
     */
    private int cacheDaysOriginal = 7;

    /**
     * 缩略图缓存天数
     */
    private int cacheDaysThumbnail = 30;

    /**
     * 支持的图片分类
     */
    private List<String> categories = Arrays.asList(
            "avatar", "bookmark", "todo", "health", "article", "other"
    );
}

