package com.pbad.health.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 饮水统计数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterStatisticsVO {
    /**
     * 总饮水量（毫升）
     */
    private Integer totalIntakeMl;

    /**
     * 总次数
     */
    private Integer totalCount;

    /**
     * 平均每次饮水量（毫升）
     */
    private Integer averageIntakeMl;

    /**
     * 达标天数（达到目标的天数）
     */
    private Integer achievementDays;

    /**
     * 按类型统计
     */
    private List<WaterTypeStatisticsVO> typeStatistics;
}

