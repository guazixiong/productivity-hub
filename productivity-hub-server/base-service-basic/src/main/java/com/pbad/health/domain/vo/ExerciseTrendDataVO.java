package com.pbad.health.domain.vo;

import lombok.Data;

/**
 * 运动趋势数据项VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseTrendDataVO {
    /**
     * 日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 数值
     */
    private Integer value;
}

