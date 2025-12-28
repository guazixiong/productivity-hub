package com.pbad.health.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.dto.ExerciseRecordUpdateDTO;
import com.pbad.health.domain.po.HealthExerciseRecordPO;
import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.domain.vo.ExerciseRecordVO;
import com.pbad.health.mapper.HealthExerciseRecordMapper;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import com.pbad.health.service.HealthExerciseRecordService;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import common.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运动记录服务实现类.
 * 关联需求：REQ-HEALTH-001, REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthExerciseRecordServiceImpl implements HealthExerciseRecordService {

    private final HealthExerciseRecordMapper exerciseRecordMapper;
    private final HealthTrainingPlanMapper trainingPlanMapper;
    private final IdGeneratorApi idGeneratorApi;

    // 运动类型枚举值
    private static final String[] EXERCISE_TYPES = {
            "跑步", "游泳", "骑行", "力量训练", "瑜伽", "有氧运动", "球类运动", "其他"
    };

    // 排序字段白名单
    private static final String[] SORT_FIELDS = {
            "exerciseDate", "durationMinutes", "caloriesBurned"
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExerciseRecordVO create(ExerciseRecordCreateDTO createDTO, String userId) {
        // 参数校验
        validateCreateDTO(createDTO);

        // 生成ID
        String id = idGeneratorApi.generateId();

        // 构建PO对象
        HealthExerciseRecordPO po = new HealthExerciseRecordPO();
        po.setId(id);
        po.setUserId(userId);
        po.setExerciseType(createDTO.getExerciseType());

        // 处理运动日期（默认当天）
        if (StringUtils.hasText(createDTO.getExerciseDate())) {
            Date exerciseDate = parseDate(createDTO.getExerciseDate());
            if (exerciseDate == null) {
                throw new BusinessException("400", "运动日期格式错误，应为yyyy-MM-dd");
            }
            po.setExerciseDate(exerciseDate);
        } else {
            // 默认当天
            po.setExerciseDate(new Date());
        }

        po.setDurationMinutes(createDTO.getDurationMinutes());
        po.setCaloriesBurned(createDTO.getCaloriesBurned());
        po.setDistanceKm(createDTO.getDistanceKm());
        po.setHeartRateAvg(createDTO.getHeartRateAvg());
        po.setHeartRateMax(createDTO.getHeartRateMax());
        po.setTrainingPlanId(createDTO.getTrainingPlanId());
        po.setNotes(createDTO.getNotes());

        // 处理训练动作参考链接（JSON数组）
        if (!CollectionUtils.isEmpty(createDTO.getExerciseActionRefUrl())) {
            po.setExerciseActionRefUrl(JSON.toJSONString(createDTO.getExerciseActionRefUrl()));
        }

        // 验证训练计划ID（如果提供）
        if (StringUtils.hasText(createDTO.getTrainingPlanId())) {
            HealthTrainingPlanPO plan = trainingPlanMapper.selectById(createDTO.getTrainingPlanId(), userId);
            if (plan == null) {
                throw new BusinessException("404", "训练计划不存在");
            }
        }

        // 设置创建时间和更新时间
        Date now = new Date();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        // 插入数据库
        int result = exerciseRecordMapper.insert(po);
        if (result <= 0) {
            throw new BusinessException("500", "创建运动记录失败");
        }

        log.info("用户{}创建运动记录成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ExerciseRecordVO> queryPage(ExerciseRecordQueryDTO queryDTO, String userId) {
        // 参数校验和默认值设置
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0
                ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0
                ? Math.min(queryDTO.getPageSize(), 100) : 20;

        // 解析日期
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            startDate = parseDate(queryDTO.getStartDate());
            if (startDate == null) {
                throw new BusinessException("400", "开始日期格式错误，应为yyyy-MM-dd");
            }
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            endDate = parseDate(queryDTO.getEndDate());
            if (endDate == null) {
                throw new BusinessException("400", "结束日期格式错误，应为yyyy-MM-dd");
            }
        }

        // 校验排序字段
        String sortField = queryDTO.getSortField();
        if (!StringUtils.hasText(sortField)) {
            sortField = "exerciseDate";
        }
        boolean validSortField = false;
        for (String field : SORT_FIELDS) {
            if (field.equals(sortField)) {
                validSortField = true;
                break;
            }
        }
        if (!validSortField) {
            sortField = "exerciseDate";
        }

        // 校验排序方向
        String sortOrder = queryDTO.getSortOrder();
        if (!StringUtils.hasText(sortOrder) || (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder))) {
            sortOrder = "desc";
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询列表
        List<HealthExerciseRecordPO> poList = exerciseRecordMapper.selectByPage(
                userId,
                queryDTO.getExerciseType(),
                startDate,
                endDate,
                queryDTO.getTrainingPlanId(),
                sortField,
                sortOrder,
                offset,
                pageSize
        );

        // 查询总数
        long total = exerciseRecordMapper.countByCondition(
                userId,
                queryDTO.getExerciseType(),
                startDate,
                endDate,
                queryDTO.getTrainingPlanId()
        );

        // 转换为VO列表
        List<ExerciseRecordVO> voList = poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(pageNum, pageSize, total, voList);
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseRecordVO getById(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        HealthExerciseRecordPO po = exerciseRecordMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "运动记录不存在");
        }

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExerciseRecordVO update(String id, ExerciseRecordUpdateDTO updateDTO, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        // 查询原记录（验证数据归属）
        HealthExerciseRecordPO existingPo = exerciseRecordMapper.selectById(id, userId);
        if (existingPo == null) {
            throw new BusinessException("404", "运动记录不存在");
        }

        // 参数校验
        validateUpdateDTO(updateDTO);

        // 更新字段
        boolean hasUpdate = false;

        if (StringUtils.hasText(updateDTO.getExerciseType())) {
            validateExerciseType(updateDTO.getExerciseType());
            existingPo.setExerciseType(updateDTO.getExerciseType());
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getExerciseDate())) {
            Date exerciseDate = parseDate(updateDTO.getExerciseDate());
            if (exerciseDate == null) {
                throw new BusinessException("400", "运动日期格式错误，应为yyyy-MM-dd");
            }
            existingPo.setExerciseDate(exerciseDate);
            hasUpdate = true;
        }

        if (updateDTO.getDurationMinutes() != null) {
            validateDurationMinutes(updateDTO.getDurationMinutes());
            existingPo.setDurationMinutes(updateDTO.getDurationMinutes());
            hasUpdate = true;
        }

        if (updateDTO.getCaloriesBurned() != null) {
            existingPo.setCaloriesBurned(updateDTO.getCaloriesBurned());
            hasUpdate = true;
        }

        if (updateDTO.getDistanceKm() != null) {
            existingPo.setDistanceKm(updateDTO.getDistanceKm());
            hasUpdate = true;
        }

        if (updateDTO.getHeartRateAvg() != null) {
            existingPo.setHeartRateAvg(updateDTO.getHeartRateAvg());
            hasUpdate = true;
        }

        if (updateDTO.getHeartRateMax() != null) {
            existingPo.setHeartRateMax(updateDTO.getHeartRateMax());
            hasUpdate = true;
        }

        if (updateDTO.getTrainingPlanId() != null) {
            if (StringUtils.hasText(updateDTO.getTrainingPlanId())) {
                // 验证训练计划是否存在
                HealthTrainingPlanPO plan = trainingPlanMapper.selectById(updateDTO.getTrainingPlanId(), userId);
                if (plan == null) {
                    throw new BusinessException("404", "训练计划不存在");
                }
                existingPo.setTrainingPlanId(updateDTO.getTrainingPlanId());
            } else {
                existingPo.setTrainingPlanId(null);
            }
            hasUpdate = true;
        }

        if (updateDTO.getExerciseActionRefUrl() != null) {
            if (CollectionUtils.isEmpty(updateDTO.getExerciseActionRefUrl())) {
                existingPo.setExerciseActionRefUrl(null);
            } else {
                existingPo.setExerciseActionRefUrl(JSON.toJSONString(updateDTO.getExerciseActionRefUrl()));
            }
            hasUpdate = true;
        }

        if (updateDTO.getNotes() != null) {
            existingPo.setNotes(updateDTO.getNotes());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BusinessException("400", "没有需要更新的字段");
        }

        // 设置更新时间
        existingPo.setUpdatedAt(new Date());

        // 更新数据库
        int result = exerciseRecordMapper.update(existingPo);
        if (result <= 0) {
            throw new BusinessException("500", "更新运动记录失败");
        }

        log.info("用户{}更新运动记录成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(existingPo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        // 验证数据归属
        HealthExerciseRecordPO po = exerciseRecordMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "运动记录不存在");
        }

        // 删除记录
        int result = exerciseRecordMapper.deleteById(id, userId);
        if (result <= 0) {
            throw new BusinessException("500", "删除运动记录失败");
        }

        log.info("用户{}删除运动记录成功，ID: {}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids, String userId) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("400", "记录ID列表不能为空");
        }

        // 批量删除
        int result = exerciseRecordMapper.batchDeleteByIds(ids, userId);
        if (result <= 0) {
            throw new BusinessException("500", "批量删除运动记录失败");
        }

        log.info("用户{}批量删除运动记录成功，删除数量: {}", userId, result);
    }

    /**
     * 校验创建DTO
     */
    private void validateCreateDTO(ExerciseRecordCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 校验运动类型（必填）
        if (!StringUtils.hasText(createDTO.getExerciseType())) {
            throw new BusinessException("400", "运动类型不能为空");
        }
        validateExerciseType(createDTO.getExerciseType());

        // 校验运动时长（必填）
        if (createDTO.getDurationMinutes() == null) {
            throw new BusinessException("400", "运动时长不能为空");
        }
        validateDurationMinutes(createDTO.getDurationMinutes());
    }

    /**
     * 校验更新DTO
     */
    private void validateUpdateDTO(ExerciseRecordUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了运动类型，需要校验
        if (StringUtils.hasText(updateDTO.getExerciseType())) {
            validateExerciseType(updateDTO.getExerciseType());
        }

        // 如果提供了运动时长，需要校验
        if (updateDTO.getDurationMinutes() != null) {
            validateDurationMinutes(updateDTO.getDurationMinutes());
        }
    }

    /**
     * 校验运动类型
     */
    private void validateExerciseType(String exerciseType) {
        boolean valid = false;
        for (String type : EXERCISE_TYPES) {
            if (type.equals(exerciseType)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "运动类型无效，可选值：跑步、游泳、骑行、力量训练、瑜伽、有氧运动、球类运动、其他");
        }
    }

    /**
     * 校验运动时长
     */
    private void validateDurationMinutes(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes < 1 || durationMinutes > 1440) {
            throw new BusinessException("400", "运动时长必须在1-1440分钟之间");
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
    private ExerciseRecordVO convertToVO(HealthExerciseRecordPO po) {
        ExerciseRecordVO vo = new ExerciseRecordVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setExerciseType(po.getExerciseType());
        vo.setExerciseDate(po.getExerciseDate());
        vo.setDurationMinutes(po.getDurationMinutes());
        vo.setCaloriesBurned(po.getCaloriesBurned());
        vo.setDistanceKm(po.getDistanceKm());
        vo.setHeartRateAvg(po.getHeartRateAvg());
        vo.setHeartRateMax(po.getHeartRateMax());
        vo.setTrainingPlanId(po.getTrainingPlanId());
        vo.setNotes(po.getNotes());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        // 解析训练动作参考链接
        if (StringUtils.hasText(po.getExerciseActionRefUrl())) {
            try {
                List<String> urls = JSON.parseArray(po.getExerciseActionRefUrl(), String.class);
                vo.setExerciseActionRefUrl(urls);
            } catch (Exception e) {
                log.warn("解析训练动作参考链接失败: {}", po.getExerciseActionRefUrl(), e);
                vo.setExerciseActionRefUrl(null);
            }
        }

        // 查询训练计划名称（如果有关联）
        if (StringUtils.hasText(po.getTrainingPlanId())) {
            try {
                HealthTrainingPlanPO plan = trainingPlanMapper.selectById(po.getTrainingPlanId(), po.getUserId());
                if (plan != null) {
                    vo.setTrainingPlanName(plan.getPlanName());
                }
            } catch (Exception e) {
                log.warn("查询训练计划名称失败: {}", po.getTrainingPlanId(), e);
            }
        }

        return vo;
    }
}

