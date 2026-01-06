package com.pbad.monitor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 监控指标VO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class MetricVO {
    /**
     * 指标名称
     */
    private String name;

    /**
     * 指标值
     */
    private Double value;

    /**
     * 指标单位
     */
    private String unit;

    /**
     * 采集时间
     */
    private LocalDateTime timestamp;

    /**
     * 指标描述
     */
    private String description;
}

