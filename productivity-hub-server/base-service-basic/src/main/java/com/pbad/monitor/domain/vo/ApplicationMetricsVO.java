package com.pbad.monitor.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 应用监控指标VO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class ApplicationMetricsVO {
    /**
     * 总请求数
     */
    private Long totalRequests;

    /**
     * 成功请求数
     */
    private Long successRequests;

    /**
     * 失败请求数
     */
    private Long errorRequests;

    /**
     * 错误率（%）
     */
    private Double errorRate;

    /**
     * 平均响应时间（ms）
     */
    private Double avgResponseTime;

    /**
     * 最大响应时间（ms）
     */
    private Double maxResponseTime;

    /**
     * 最小响应时间（ms）
     */
    private Double minResponseTime;

    /**
     * QPS（每秒请求数）
     */
    private Double qps;

    /**
     * 接口统计（按接口路径分组）
     */
    private Map<String, ApiStatVO> apiStats;

    /**
     * 所有指标列表
     */
    private List<MetricVO> metrics;
}

