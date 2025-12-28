package com.pbad.health.service;

import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.dto.ExerciseRecordUpdateDTO;
import com.pbad.health.domain.vo.ExerciseRecordVO;
import common.core.domain.PageResult;

import java.util.List;

/**
 * 运动记录服务接口.
 * 关联需求：REQ-HEALTH-001, REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthExerciseRecordService {

    /**
     * 创建运动记录.
     * 关联接口：API-REQ-HEALTH-001-01
     *
     * @param createDTO 创建DTO
     * @param userId    用户ID
     * @return 运动记录VO
     */
    ExerciseRecordVO create(ExerciseRecordCreateDTO createDTO, String userId);

    /**
     * 分页查询运动记录列表.
     * 关联接口：API-REQ-HEALTH-001-02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 分页结果
     */
    PageResult<ExerciseRecordVO> queryPage(ExerciseRecordQueryDTO queryDTO, String userId);

    /**
     * 根据ID查询运动记录详情.
     * 关联接口：API-REQ-HEALTH-001-03
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 运动记录VO
     */
    ExerciseRecordVO getById(String id, String userId);

    /**
     * 更新运动记录.
     * 关联接口：API-REQ-HEALTH-001-04
     *
     * @param id        记录ID
     * @param updateDTO 更新DTO
     * @param userId    用户ID
     * @return 运动记录VO
     */
    ExerciseRecordVO update(String id, ExerciseRecordUpdateDTO updateDTO, String userId);

    /**
     * 删除运动记录.
     * 关联接口：API-REQ-HEALTH-001-05
     *
     * @param id     记录ID
     * @param userId 用户ID
     */
    void delete(String id, String userId);

    /**
     * 批量删除运动记录.
     * 关联接口：API-REQ-HEALTH-001-06
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     */
    void batchDelete(List<String> ids, String userId);
}

