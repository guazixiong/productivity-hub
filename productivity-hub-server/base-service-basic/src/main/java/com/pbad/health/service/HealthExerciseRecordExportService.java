package com.pbad.health.service;

import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;

/**
 * 运动记录导出服务接口.
 * 关联需求：REQ-HEALTH-001
 * 关联任务：TASK-013
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthExerciseRecordExportService {

    /**
     * 导出运动记录为Excel格式.
     * 关联接口：API-REQ-HEALTH-001-07
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(ExerciseRecordQueryDTO queryDTO, String userId);

    /**
     * 导出运动记录为CSV格式.
     * 关联接口：API-REQ-HEALTH-001-08
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return CSV文件字节数组
     */
    byte[] exportToCsv(ExerciseRecordQueryDTO queryDTO, String userId);
}

