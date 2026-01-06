package com.pbad.health.service;

import com.pbad.health.domain.dto.TrainingPlanCreateDTO;
import com.pbad.health.domain.dto.TrainingPlanQueryDTO;
import com.pbad.health.domain.dto.TrainingPlanUpdateDTO;
import com.pbad.health.domain.vo.TrainingPlanVO;

import java.util.List;

/**
 * 训练计划服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthTrainingPlanService {

    /**
     * 创建训练计划.
01
     *
     * @param createDTO 创建DTO
     * @param userId    用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO create(TrainingPlanCreateDTO createDTO, String userId);

    /**
     * 查询训练计划列表.
02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 训练计划列表
     */
    List<TrainingPlanVO> queryList(TrainingPlanQueryDTO queryDTO, String userId);

    /**
     * 根据ID查询训练计划详情.
03
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO getById(String id, String userId);

    /**
     * 更新训练计划.
04
     *
     * @param id        计划ID
     * @param updateDTO 更新DTO
     * @param userId    用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO update(String id, TrainingPlanUpdateDTO updateDTO, String userId);

    /**
     * 暂停训练计划.
05
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO pause(String id, String userId);

    /**
     * 恢复训练计划.
06
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO resume(String id, String userId);

    /**
     * 完成训练计划.
07
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 训练计划VO
     */
    TrainingPlanVO complete(String id, String userId);

    /**
     * 删除训练计划.
08
     *
     * @param id     计划ID
     * @param userId 用户ID
     */
    void delete(String id, String userId);
}

