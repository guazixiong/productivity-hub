package com.pbad.health.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 运动趋势数据VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseTrendVO {
    /**
     * 趋势类型（duration/count/calories）
     */
    private String type;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 趋势数据列表
     */
    private List<ExerciseTrendDataVO> data;
}

