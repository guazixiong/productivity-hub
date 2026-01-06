package com.pbad.monitor.domain.dto;

import lombok.Data;

/**
 * 告警规则DTO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class AlertRuleDTO {
    /**
     * 规则ID
     */
    private String id;

    /**
     * 指标名称（如：cpu_usage, memory_usage, error_rate等）
     */
    private String metricName;

    /**
     * 告警阈值
     */
    private Double threshold;

    /**
     * 比较运算符（>、>=、<、<=、==）
     */
    private String operator;

    /**
     * 告警级别（INFO、WARN、ERROR、CRITICAL）
     */
    private String level;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 告警消息模板
     */
    private String messageTemplate;
}

