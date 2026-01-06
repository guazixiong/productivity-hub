package com.pbad.monitor.service;

import com.pbad.monitor.domain.vo.ApplicationMetricsVO;
import com.pbad.monitor.domain.vo.SystemMetricsVO;

/**
 * 监控服务接口
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
public interface MonitorService {

    /**
     * 获取系统监控指标
     *
     * @return 系统监控指标
     */
    SystemMetricsVO getSystemMetrics();

    /**
     * 获取应用监控指标
     *
     * @return 应用监控指标
     */
    ApplicationMetricsVO getApplicationMetrics();
}

