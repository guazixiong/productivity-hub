package com.pbad.health.service.impl;

import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import com.pbad.health.service.HealthBusinessRuleService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 健康模块业务规则校验服务实现类.
 * 关联需求：REQ-HEALTH-003
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthBusinessRuleServiceImpl implements HealthBusinessRuleService {

    private final HealthTrainingPlanMapper trainingPlanMapper;

    // 状态枚举值
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Override
    public void validatePauseStatusTransition(HealthTrainingPlanPO plan) {
        if (plan == null) {
            throw new BusinessException("400", "训练计划不能为空");
        }
        if (!STATUS_ACTIVE.equals(plan.getStatus())) {
            throw new BusinessException("400", "只有进行中的训练计划可以暂停");
        }
    }

    @Override
    public void validateResumeStatusTransition(HealthTrainingPlanPO plan) {
        if (plan == null) {
            throw new BusinessException("400", "训练计划不能为空");
        }
        if (!STATUS_PAUSED.equals(plan.getStatus())) {
            throw new BusinessException("400", "只有已暂停的训练计划可以恢复");
        }
    }

    @Override
    public void validateCompleteStatusTransition(HealthTrainingPlanPO plan) {
        if (plan == null) {
            throw new BusinessException("400", "训练计划不能为空");
        }
        if (!STATUS_ACTIVE.equals(plan.getStatus()) && !STATUS_PAUSED.equals(plan.getStatus())) {
            throw new BusinessException("400", "只有进行中或已暂停的训练计划可以完成");
        }
    }

    @Override
    public void validateCancelStatusTransition(HealthTrainingPlanPO plan) {
        if (plan == null) {
            throw new BusinessException("400", "训练计划不能为空");
        }
        if (!STATUS_ACTIVE.equals(plan.getStatus()) && !STATUS_PAUSED.equals(plan.getStatus())) {
            throw new BusinessException("400", "只有进行中或已暂停的训练计划可以取消");
        }
    }

    @Override
    public void validatePlanCanUpdate(HealthTrainingPlanPO plan) {
        if (plan == null) {
            throw new BusinessException("400", "训练计划不能为空");
        }
        if (STATUS_COMPLETED.equals(plan.getStatus()) || STATUS_CANCELLED.equals(plan.getStatus())) {
            throw new BusinessException("400", "已完成或已取消的训练计划不能更新");
        }
    }

    @Override
    public long checkTrainingPlanAssociation(String planId, String userId) {
        if (!StringUtils.hasText(planId)) {
            throw new BusinessException("400", "训练计划ID不能为空");
        }
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("400", "用户ID不能为空");
        }
        return trainingPlanMapper.countExerciseRecords(planId, userId);
    }

    @Override
    public void validatePlanCanDelete(String planId, String userId) {
        if (!StringUtils.hasText(planId)) {
            throw new BusinessException("400", "训练计划ID不能为空");
        }
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("400", "用户ID不能为空");
        }
        long recordCount = checkTrainingPlanAssociation(planId, userId);
        if (recordCount > 0) {
            throw new BusinessException("400", "该训练计划下存在运动记录，无法删除");
        }
    }

    @Override
    public void validateDataOwnership(String dataUserId, String currentUserId, String dataType) {
        if (!StringUtils.hasText(dataUserId)) {
            throw new BusinessException("400", "数据用户ID不能为空");
        }
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException("400", "当前用户ID不能为空");
        }
        if (!dataUserId.equals(currentUserId)) {
            String typeName = StringUtils.hasText(dataType) ? dataType : "数据";
            throw new BusinessException("403", typeName + "不属于当前用户，无权操作");
        }
    }
}

