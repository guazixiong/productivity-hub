package com.pbad.health.domain.vo;

import lombok.Data;

/**
 * 饮水趋势数据项VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTrendDataVO {
    /**
     * 日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 总饮水量（毫升）
     */
    private Integer totalIntakeMl;

    /**
     * 达标百分比
     */
    private Double achievementPercent;

    /**
     * 是否达标
     */
    private Boolean isAchieved;
}

