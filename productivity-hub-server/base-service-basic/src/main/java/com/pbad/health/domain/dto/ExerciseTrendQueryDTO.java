package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 运动趋势查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class ExerciseTrendQueryDTO {
    /**
     * 趋势类型（duration/count/calories），默认duration
     */
    private String type = "duration";

    /**
     * 天数（7/30/90），默认7
     */
    private Integer days = 7;
}

