package com.pbad.image.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片统计视图对象.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class ImageStatisticsVO {

    /**
     * 图片总数
     */
    private Long totalCount;

    /**
     * 总存储空间（字节）
     */
    private Long totalSize;

    /**
     * 平均文件大小（字节）
     */
    private Long averageSize;

    /**
     * 最大文件大小（字节）
     */
    private Long maxFileSize;

    /**
     * 最小文件大小（字节）
     */
    private Long minFileSize;

    /**
     * 分类统计列表
     */
    private List<CategoryStat> categoryStats;

    /**
     * 访问统计
     */
    private AccessStat accessStats;

    /**
     * 热门图片列表（访问次数最多的前10张）
     */
    private List<ImageVO> hotImages;

    /**
     * 上传趋势（按日期统计）
     */
    private List<TrendItem> uploadTrend;

    /**
     * 访问趋势（按日期统计）
     */
    private List<TrendItem> accessTrend;

    /**
     * 分类统计项
     */
    @Data
    @NoArgsConstructor
    public static class CategoryStat {
        /**
         * 分类
         */
        private String category;

        /**
         * 数量
         */
        private Long count;

        /**
         * 总大小（字节）
         */
        private Long totalSize;
    }

    /**
     * 访问统计项
     */
    @Data
    @NoArgsConstructor
    public static class AccessStat {
        /**
         * 总访问次数
         */
        private Long totalAccessCount;

        /**
         * 平均访问次数
         */
        private Long averageAccessCount;

        /**
         * 最大访问次数
         */
        private Long maxAccessCount;
    }

    /**
     * 趋势项
     */
    @Data
    @NoArgsConstructor
    public static class TrendItem {
        /**
         * 日期（yyyy-MM-dd）
         */
        private String date;

        /**
         * 数量
         */
        private Long count;
    }
}

