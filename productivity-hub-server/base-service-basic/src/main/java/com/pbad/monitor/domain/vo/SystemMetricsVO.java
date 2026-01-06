package com.pbad.monitor.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 系统监控指标VO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class SystemMetricsVO {
    /**
     * CPU使用率（%）
     */
    private Double cpuUsage;

    /**
     * 内存使用率（%）
     */
    private Double memoryUsage;

    /**
     * 堆内存使用（MB）
     */
    private Long heapMemoryUsed;

    /**
     * 堆内存总量（MB）
     */
    private Long heapMemoryTotal;

    /**
     * 非堆内存使用（MB）
     */
    private Long nonHeapMemoryUsed;

    /**
     * 磁盘使用率（%）
     */
    private Double diskUsage;

    /**
     * 线程数
     */
    private Integer threadCount;

    /**
     * 活跃线程数
     */
    private Integer activeThreadCount;

    /**
     * JVM运行时间（秒）
     */
    private Long uptime;

    /**
     * 所有指标列表
     */
    private List<MetricVO> metrics;
}

