package com.pbad.asset.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 心愿单卡片统计信息视图对象（VO）.
 *
 * 对应前端 WishlistCardStatistics：
 * - totalCount: 心愿总数（不限时间）
 * - totalValue: 心愿总价值（不限时间）
 * - addedCount: 统计时间范围内新增的心愿数量
 * - achievedCount: 统计时间范围内实现的心愿数量
 */
@Data
public class WishlistCardStatisticsVO {

    /**
     * 心愿总数（不限时间）
     */
    private Integer totalCount;

    /**
     * 心愿总价值（不限时间）
     */
    private BigDecimal totalValue;

    /**
     * 统计时间范围内新增的心愿数量
     */
    private Integer addedCount;

    /**
     * 统计时间范围内实现的心愿数量
     */
    private Integer achievedCount;
}

