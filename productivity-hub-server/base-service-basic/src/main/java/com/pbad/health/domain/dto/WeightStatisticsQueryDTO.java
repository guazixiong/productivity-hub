package com.pbad.health.domain.dto;

import lombok.Data;

/**
 * 体重统计查询DTO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WeightStatisticsQueryDTO {
    /**
     * 统计周期（week/month/quarter），默认week
     */
    private String period = "week";

    /**
     * 开始日期（yyyy-MM-dd），period=custom时必填
     */
    private String startDate;

    /**
     * 结束日期（yyyy-MM-dd），period=custom时必填
     */
    private String endDate;
}

