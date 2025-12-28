package com.pbad.health.service;

import com.pbad.health.domain.dto.WaterIntakeQueryDTO;

/**
 * 饮水记录导出服务接口.
 * 关联需求：REQ-HEALTH-003
 * 关联任务：TASK-024
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWaterIntakeExportService {

    /**
     * 导出饮水记录为Excel格式.
     * 关联接口：API-REQ-HEALTH-003-07
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(WaterIntakeQueryDTO queryDTO, String userId);

    /**
     * 导出饮水记录为CSV格式.
     * 关联接口：API-REQ-HEALTH-003-08
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return CSV文件字节数组
     */
    byte[] exportToCsv(WaterIntakeQueryDTO queryDTO, String userId);
}

