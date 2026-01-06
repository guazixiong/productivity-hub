package com.pbad.monitor.domain.vo;

import lombok.Data;

/**
 * 接口统计VO
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Data
public class ApiStatVO {
    /**
     * 接口路径
     */
    private String path;

    /**
     * 请求次数
     */
    private Long requestCount;

    /**
     * 成功次数
     */
    private Long successCount;

    /**
     * 失败次数
     */
    private Long errorCount;

    /**
     * 平均响应时间（ms）
     */
    private Double avgResponseTime;

    /**
     * 最大响应时间（ms）
     */
    private Double maxResponseTime;
}

