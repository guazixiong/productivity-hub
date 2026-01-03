package com.pbad.health.service;

import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;

/**
 * 运动记录导出服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthExerciseRecordExportService {

    /**
     * 导出运动记录为Excel格式.
7
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return Excel文件字节数组
     */
    byte[] exportToExcel(ExerciseRecordQueryDTO queryDTO, String userId);

    /**
     * 导出运动记录为CSV格式.
8
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return CSV文件字节数组
     */
    byte[] exportToCsv(ExerciseRecordQueryDTO queryDTO, String userId);
}

