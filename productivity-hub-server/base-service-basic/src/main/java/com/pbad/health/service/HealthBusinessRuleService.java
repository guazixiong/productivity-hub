package com.pbad.health.service;

import com.pbad.health.domain.po.HealthTrainingPlanPO;

/**
 * 健康模块业务规则校验服务接口.
 * 关联需求：REQ-HEALTH-003
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthBusinessRuleService {

    /**
     * 校验训练计划状态流转（暂停）
     * 只有ACTIVE状态可以暂停
     *
     * @param plan 训练计划
     * @throws common.exception.BusinessException 如果状态流转不合法
     */
    void validatePauseStatusTransition(HealthTrainingPlanPO plan);

    /**
     * 校验训练计划状态流转（恢复）
     * 只有PAUSED状态可以恢复
     *
     * @param plan 训练计划
     * @throws common.exception.BusinessException 如果状态流转不合法
     */
    void validateResumeStatusTransition(HealthTrainingPlanPO plan);

    /**
     * 校验训练计划状态流转（完成）
     * 只有ACTIVE或PAUSED状态可以完成
     *
     * @param plan 训练计划
     * @throws common.exception.BusinessException 如果状态流转不合法
     */
    void validateCompleteStatusTransition(HealthTrainingPlanPO plan);

    /**
     * 校验训练计划状态流转（取消）
     * 只有ACTIVE或PAUSED状态可以取消
     *
     * @param plan 训练计划
     * @throws common.exception.BusinessException 如果状态流转不合法
     */
    void validateCancelStatusTransition(HealthTrainingPlanPO plan);

    /**
     * 校验训练计划是否可以更新
     * 已完成或已取消的计划不能更新
     *
     * @param plan 训练计划
     * @throws common.exception.BusinessException 如果计划不能更新
     */
    void validatePlanCanUpdate(HealthTrainingPlanPO plan);

    /**
     * 校验训练计划关联关系
     * 检查训练计划是否有关联的运动记录
     *
     * @param planId 训练计划ID
     * @param userId 用户ID
     * @return 关联的运动记录数量
     */
    long checkTrainingPlanAssociation(String planId, String userId);

    /**
     * 校验训练计划是否可以删除
     * 如果有关联的运动记录，不允许删除
     *
     * @param planId 训练计划ID
     * @param userId 用户ID
     * @throws common.exception.BusinessException 如果计划不能删除
     */
    void validatePlanCanDelete(String planId, String userId);

    /**
     * 校验数据归属
     * 检查数据是否属于指定用户
     *
     * @param dataUserId 数据所属用户ID
     * @param currentUserId 当前用户ID
     * @param dataType 数据类型（用于错误提示）
     * @throws common.exception.BusinessException 如果数据不属于当前用户
     */
    void validateDataOwnership(String dataUserId, String currentUserId, String dataType);
}

