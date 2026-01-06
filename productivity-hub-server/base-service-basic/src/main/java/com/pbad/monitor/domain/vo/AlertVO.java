package com.pbad.monitor.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 告警VO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class AlertVO {
    /**
     * 告警ID
     */
    private String id;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标值
     */
    private Double metricValue;

    /**
     * 告警级别
     */
    private String level;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 告警时间
     */
    private LocalDateTime alertTime;

    /**
     * 是否已处理
     */
    private Boolean handled;

    /**
     * 处理时间
     */
    private LocalDateTime handledTime;
}

