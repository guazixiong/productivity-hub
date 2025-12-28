package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.TrainingPlanCreateDTO;
import com.pbad.health.domain.dto.TrainingPlanQueryDTO;
import com.pbad.health.domain.dto.TrainingPlanUpdateDTO;
import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.domain.vo.TrainingPlanVO;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import com.pbad.health.service.HealthTrainingPlanService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 训练计划服务实现类.
 * 关联需求：REQ-HEALTH-003
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthTrainingPlanServiceImpl implements HealthTrainingPlanService {

    private final HealthTrainingPlanMapper trainingPlanMapper;
    private final IdGeneratorApi idGeneratorApi;

    // 计划类型枚举值
    private static final String[] PLAN_TYPES = {
            "减脂", "增肌", "塑形", "耐力提升", "康复训练", "其他"
    };

    // 状态枚举值
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlanVO create(TrainingPlanCreateDTO createDTO, String userId) {
        // 参数校验
        validateCreateDTO(createDTO);

        // 生成ID
        String id = idGeneratorApi.generateId();

        // 构建PO对象
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(id);
        po.setUserId(userId);
        po.setPlanName(createDTO.getPlanName());
        po.setPlanType(createDTO.getPlanType());
        po.setTargetDurationDays(createDTO.getTargetDurationDays());
        po.setTargetCaloriesPerDay(createDTO.getTargetCaloriesPerDay());
        po.setDescription(createDTO.getDescription());

        // 处理日期
        if (StringUtils.hasText(createDTO.getStartDate())) {
            Date startDate = parseDate(createDTO.getStartDate());
            if (startDate == null) {
                throw new BusinessException("400", "开始日期格式错误，应为yyyy-MM-dd");
            }
            po.setStartDate(startDate);
        }

        if (StringUtils.hasText(createDTO.getEndDate())) {
            Date endDate = parseDate(createDTO.getEndDate());
            if (endDate == null) {
                throw new BusinessException("400", "结束日期格式错误，应为yyyy-MM-dd");
            }
            po.setEndDate(endDate);
        }

        // 验证日期逻辑
        if (po.getStartDate() != null && po.getEndDate() != null) {
            if (po.getEndDate().before(po.getStartDate())) {
                throw new BusinessException("400", "结束日期不能早于开始日期");
            }
        }

        // 默认状态为ACTIVE
        po.setStatus(STATUS_ACTIVE);

        // 设置创建时间和更新时间
        Date now = new Date();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        // 插入数据库
        int result = trainingPlanMapper.insert(po);
        if (result <= 0) {
            throw new BusinessException("500", "创建训练计划失败");
        }

        log.info("用户{}创建训练计划成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(po);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingPlanVO> queryList(TrainingPlanQueryDTO queryDTO, String userId) {
        // 查询列表
        List<HealthTrainingPlanPO> poList = trainingPlanMapper.selectList(
                userId,
                queryDTO.getStatus(),
                queryDTO.getPlanType()
        );

        // 转换为VO列表
        return poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingPlanVO getById(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        HealthTrainingPlanPO po = trainingPlanMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlanVO update(String id, TrainingPlanUpdateDTO updateDTO, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        // 查询原记录（验证数据归属）
        HealthTrainingPlanPO existingPo = trainingPlanMapper.selectById(id, userId);
        if (existingPo == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        // 状态校验：已完成或已取消的计划不能更新
        if (STATUS_COMPLETED.equals(existingPo.getStatus()) || STATUS_CANCELLED.equals(existingPo.getStatus())) {
            throw new BusinessException("400", "已完成或已取消的训练计划不能更新");
        }

        // 参数校验
        validateUpdateDTO(updateDTO);

        // 更新字段
        boolean hasUpdate = false;

        if (StringUtils.hasText(updateDTO.getPlanName())) {
            if (updateDTO.getPlanName().length() > 128) {
                throw new BusinessException("400", "计划名称长度不能超过128字符");
            }
            existingPo.setPlanName(updateDTO.getPlanName());
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getPlanType())) {
            validatePlanType(updateDTO.getPlanType());
            existingPo.setPlanType(updateDTO.getPlanType());
            hasUpdate = true;
        }

        if (updateDTO.getTargetDurationDays() != null) {
            existingPo.setTargetDurationDays(updateDTO.getTargetDurationDays());
            hasUpdate = true;
        }

        if (updateDTO.getTargetCaloriesPerDay() != null) {
            existingPo.setTargetCaloriesPerDay(updateDTO.getTargetCaloriesPerDay());
            hasUpdate = true;
        }

        if (updateDTO.getDescription() != null) {
            existingPo.setDescription(updateDTO.getDescription());
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getStartDate())) {
            Date startDate = parseDate(updateDTO.getStartDate());
            if (startDate == null) {
                throw new BusinessException("400", "开始日期格式错误，应为yyyy-MM-dd");
            }
            existingPo.setStartDate(startDate);
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getEndDate())) {
            Date endDate = parseDate(updateDTO.getEndDate());
            if (endDate == null) {
                throw new BusinessException("400", "结束日期格式错误，应为yyyy-MM-dd");
            }
            existingPo.setEndDate(endDate);
            hasUpdate = true;
        }

        // 验证日期逻辑
        if (existingPo.getStartDate() != null && existingPo.getEndDate() != null) {
            if (existingPo.getEndDate().before(existingPo.getStartDate())) {
                throw new BusinessException("400", "结束日期不能早于开始日期");
            }
        }

        if (!hasUpdate) {
            throw new BusinessException("400", "没有需要更新的字段");
        }

        // 设置更新时间
        existingPo.setUpdatedAt(new Date());

        // 更新数据库
        int result = trainingPlanMapper.update(existingPo);
        if (result <= 0) {
            throw new BusinessException("500", "更新训练计划失败");
        }

        log.info("用户{}更新训练计划成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(existingPo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlanVO pause(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        HealthTrainingPlanPO po = trainingPlanMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        // 状态流转校验：只有ACTIVE状态可以暂停
        if (!STATUS_ACTIVE.equals(po.getStatus())) {
            throw new BusinessException("400", "只有进行中的训练计划可以暂停");
        }

        // 更新状态
        po.setStatus(STATUS_PAUSED);
        po.setUpdatedAt(new Date());

        int result = trainingPlanMapper.update(po);
        if (result <= 0) {
            throw new BusinessException("500", "暂停训练计划失败");
        }

        log.info("用户{}暂停训练计划成功，ID: {}", userId, id);

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlanVO resume(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        HealthTrainingPlanPO po = trainingPlanMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        // 状态流转校验：只有PAUSED状态可以恢复
        if (!STATUS_PAUSED.equals(po.getStatus())) {
            throw new BusinessException("400", "只有已暂停的训练计划可以恢复");
        }

        // 更新状态
        po.setStatus(STATUS_ACTIVE);
        po.setUpdatedAt(new Date());

        int result = trainingPlanMapper.update(po);
        if (result <= 0) {
            throw new BusinessException("500", "恢复训练计划失败");
        }

        log.info("用户{}恢复训练计划成功，ID: {}", userId, id);

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TrainingPlanVO complete(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        HealthTrainingPlanPO po = trainingPlanMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        // 状态流转校验：ACTIVE或PAUSED状态可以完成
        if (!STATUS_ACTIVE.equals(po.getStatus()) && !STATUS_PAUSED.equals(po.getStatus())) {
            throw new BusinessException("400", "只有进行中或已暂停的训练计划可以完成");
        }

        // 更新状态
        po.setStatus(STATUS_COMPLETED);
        po.setUpdatedAt(new Date());

        int result = trainingPlanMapper.update(po);
        if (result <= 0) {
            throw new BusinessException("500", "完成训练计划失败");
        }

        log.info("用户{}完成训练计划成功，ID: {}", userId, id);

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "计划ID不能为空");
        }

        // 验证数据归属
        HealthTrainingPlanPO po = trainingPlanMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "训练计划不存在");
        }

        // 检查关联关系：如果有关联的运动记录，不允许删除
        long recordCount = trainingPlanMapper.countExerciseRecords(id, userId);
        if (recordCount > 0) {
            throw new BusinessException("400", "该训练计划下存在运动记录，无法删除");
        }

        // 删除记录
        int result = trainingPlanMapper.deleteById(id, userId);
        if (result <= 0) {
            throw new BusinessException("500", "删除训练计划失败");
        }

        log.info("用户{}删除训练计划成功，ID: {}", userId, id);
    }

    /**
     * 校验创建DTO
     */
    private void validateCreateDTO(TrainingPlanCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 校验计划名称（必填）
        if (!StringUtils.hasText(createDTO.getPlanName())) {
            throw new BusinessException("400", "计划名称不能为空");
        }
        if (createDTO.getPlanName().length() > 128) {
            throw new BusinessException("400", "计划名称长度不能超过128字符");
        }

        // 校验计划类型（必填）
        if (!StringUtils.hasText(createDTO.getPlanType())) {
            throw new BusinessException("400", "计划类型不能为空");
        }
        validatePlanType(createDTO.getPlanType());
    }

    /**
     * 校验更新DTO
     */
    private void validateUpdateDTO(TrainingPlanUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了计划类型，需要校验
        if (StringUtils.hasText(updateDTO.getPlanType())) {
            validatePlanType(updateDTO.getPlanType());
        }
    }

    /**
     * 校验计划类型
     */
    private void validatePlanType(String planType) {
        boolean valid = false;
        for (String type : PLAN_TYPES) {
            if (type.equals(planType)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "计划类型无效，可选值：减脂、增肌、塑形、耐力提升、康复训练、其他");
        }
    }

    /**
     * 解析日期字符串（yyyy-MM-dd格式）
     */
    private Date parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr.trim());
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 转换为VO对象
     */
    private TrainingPlanVO convertToVO(HealthTrainingPlanPO po) {
        TrainingPlanVO vo = new TrainingPlanVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setPlanName(po.getPlanName());
        vo.setPlanType(po.getPlanType());
        vo.setTargetDurationDays(po.getTargetDurationDays());
        vo.setTargetCaloriesPerDay(po.getTargetCaloriesPerDay());
        vo.setDescription(po.getDescription());
        vo.setStatus(po.getStatus());
        vo.setStartDate(po.getStartDate());
        vo.setEndDate(po.getEndDate());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        // 查询关联的运动记录数
        try {
            long recordCount = trainingPlanMapper.countExerciseRecords(po.getId(), po.getUserId());
            vo.setExerciseRecordCount(recordCount);
        } catch (Exception e) {
            log.warn("查询训练计划关联记录数失败: {}", po.getId(), e);
            vo.setExerciseRecordCount(0L);
        }

        return vo;
    }
}

