package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 饮水统计查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WaterStatisticsQueryDTO {
    /**
     * 统计周期（today/week/month/all/custom），默认today
     */
    private String period = "today";

    /**
     * 开始日期（yyyy-MM-dd），period=custom时必填
     */
    private String startDate;

    /**
     * 结束日期（yyyy-MM-dd），period=custom时必填
     */
    private String endDate;
}

