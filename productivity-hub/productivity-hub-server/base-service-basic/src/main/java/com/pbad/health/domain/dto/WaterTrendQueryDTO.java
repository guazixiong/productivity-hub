package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 饮水趋势查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterTrendQueryDTO {
    /**
     * 天数（7/30/90），默认7
     */
    private Integer days = 7;
}

