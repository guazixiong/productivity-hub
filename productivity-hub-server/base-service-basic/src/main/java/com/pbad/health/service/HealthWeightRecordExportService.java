package com.pbad.health.service;

import com.pbad.health.domain.dto.WeightRecordQueryDTO;

/**
 * 体重记录导出服务接口.
 * 关联需求：REQ-HEALTH-005
 * 关联任务：TASK-035
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWeightRecordExportService {

    /**
     * 导出体重记录为Excel格式.
     * 关联接口：API-REQ-HEALTH-005-07
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(WeightRecordQueryDTO queryDTO, String userId);

    /**
     * 导出体重记录为CSV格式.
     * 关联接口：API-REQ-HEALTH-005-08
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return CSV文件字节数组
     */
    byte[] exportToCsv(WeightRecordQueryDTO queryDTO, String userId);
}

