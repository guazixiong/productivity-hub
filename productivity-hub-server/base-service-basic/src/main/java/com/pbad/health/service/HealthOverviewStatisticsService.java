package com.pbad.health.service;

import com.pbad.health.domain.vo.HealthCalendarVO;
import com.pbad.health.domain.vo.HealthOverviewVO;

/**
 * 综合统计服务接口.
 * 关联需求：REQ-HEALTH-004
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthOverviewStatisticsService {

    /**
     * 查询健康数据概览.
     * 关联接口：API-REQ-HEALTH-004-01
     *
     * @param period 统计周期（today/week/month），默认today
     * @param userId 用户ID
     * @return 健康数据概览
     */
    HealthOverviewVO getOverview(String period, String userId);

    /**
     * 查询健康数据日历.
     * 关联接口：API-REQ-HEALTH-004-02
     *
     * @param year   年份，默认当前年份
     * @param month  月份（1-12），默认当前月份
     * @param userId 用户ID
     * @return 健康数据日历
     */
    HealthCalendarVO getCalendar(Integer year, Integer month, String userId);
}

