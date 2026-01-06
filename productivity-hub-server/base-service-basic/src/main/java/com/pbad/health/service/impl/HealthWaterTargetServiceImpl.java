package com.pbad.health.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.WaterTargetCreateOrUpdateDTO;
import com.pbad.health.domain.po.HealthWaterIntakePO;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.domain.vo.WaterTargetProgressVO;
import com.pbad.health.domain.vo.WaterTargetVO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import com.pbad.health.service.HealthWaterTargetService;
import common.exception.BusinessException;
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

/**
 * 饮水目标服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWaterTargetServiceImpl implements HealthWaterTargetService {

    private final HealthWaterTargetMapper waterTargetMapper;
    private final HealthWaterIntakeMapper waterIntakeMapper;
    private final IdGeneratorApi idGeneratorApi;

    // 默认每日目标饮水量（毫升）
    private static final int DEFAULT_DAILY_TARGET_ML = 2000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaterTargetVO createOrUpdate(WaterTargetCreateOrUpdateDTO dto, String userId) {
        // 参数校验
        validateDTO(dto);

        // 查询是否已存在
        HealthWaterTargetPO existingPo = waterTargetMapper.selectByUserId(userId);

        Date now = new Date();

        if (existingPo != null) {
            // 更新现有记录
            boolean hasUpdate = false;

            if (dto.getDailyTargetMl() != null) {
                validateDailyTargetMl(dto.getDailyTargetMl());
                existingPo.setDailyTargetMl(dto.getDailyTargetMl());
                hasUpdate = true;
            }

            if (dto.getReminderIntervals() != null) {
                if (CollectionUtils.isEmpty(dto.getReminderIntervals())) {
                    existingPo.setReminderIntervals(null);
                } else {
                    existingPo.setReminderIntervals(JSON.toJSONString(dto.getReminderIntervals()));
                }
                hasUpdate = true;
            }

            if (!hasUpdate) {
                throw new BusinessException("400", "没有需要更新的字段");
            }

            existingPo.setUpdatedAt(now);

            int result = waterTargetMapper.update(existingPo);
            if (result <= 0) {
                throw new BusinessException("500", "更新饮水目标失败");
            }

            log.info("用户{}更新饮水目标成功", userId);
            return convertToVO(existingPo);
        } else {
            // 创建新记录
            String id = idGeneratorApi.generateId();

            HealthWaterTargetPO po = new HealthWaterTargetPO();
            po.setId(id);
            po.setUserId(userId);
            po.setDailyTargetMl(dto.getDailyTargetMl() != null ? dto.getDailyTargetMl() : DEFAULT_DAILY_TARGET_ML);
            po.setReminderEnabled(0); // 默认不启用，是否提醒由定时任务开关控制
            po.setReminderIntervals(dto.getReminderIntervals() != null && !CollectionUtils.isEmpty(dto.getReminderIntervals())
                    ? JSON.toJSONString(dto.getReminderIntervals()) : null);
            po.setCreatedAt(now);
            po.setUpdatedAt(now);

            int result = waterTargetMapper.insert(po);
            if (result <= 0) {
                throw new BusinessException("500", "创建饮水目标失败");
            }

            log.info("用户{}创建饮水目标成功，ID: {}", userId, id);
            return convertToVO(po);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WaterTargetVO getByUserId(String userId) {
        HealthWaterTargetPO po = waterTargetMapper.selectByUserId(userId);

        if (po != null) {
            return convertToVO(po);
        } else {
            // 返回默认值
            WaterTargetVO vo = new WaterTargetVO();
            vo.setId(null);
            vo.setUserId(userId);
            vo.setDailyTargetMl(DEFAULT_DAILY_TARGET_ML);
            vo.setReminderEnabled(false); // 是否提醒由定时任务开关控制
            vo.setReminderIntervals(null);
            vo.setCreatedAt(null);
            vo.setUpdatedAt(null);
            return vo;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WaterTargetProgressVO getProgress(String userId, String queryDate) {
        // 解析查询日期（默认当天）
        Date date;
        if (StringUtils.hasText(queryDate)) {
            date = parseDate(queryDate);
            if (date == null) {
                throw new BusinessException("400", "查询日期格式错误，应为yyyy-MM-dd");
            }
        } else {
            // 使用当天的开始时间（00:00:00），而不是当前时间
            LocalDate today = LocalDate.now();
            date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        // 确保 date 是当天的开始时间（00:00:00），用于与数据库的 date 类型字段比较
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date dateStart = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 查询饮水目标配置
        HealthWaterTargetPO targetPo = waterTargetMapper.selectByUserId(userId);
        int targetMl = targetPo != null ? targetPo.getDailyTargetMl() : DEFAULT_DAILY_TARGET_ML;

        // 查询当日总饮水量（使用当天的开始时间进行查询）
        Integer consumedMl = waterIntakeMapper.sumVolumeByDate(userId, dateStart);
        if (consumedMl == null) {
            consumedMl = 0;
        }

        // 计算当天的开始和结束时间（用于统计当日饮水次数）
        // 使用当天的开始时间（00:00:00）和结束时间（23:59:59.999）
        Date startDate = dateStart; // 已经计算好的当天开始时间
        // 使用当天的最后一刻（23:59:59.999），而不是下一天的开始时间
        Date endDate = Date.from(localDate.atTime(23, 59, 59, 999_000_000).atZone(ZoneId.systemDefault()).toInstant());

        // 统计当日饮水次数
        long intakeCount = waterIntakeMapper.countByCondition(userId, null, startDate, endDate);

        // 计算剩余需饮水量
        int remainingMl = Math.max(0, targetMl - consumedMl);

        // 计算完成进度百分比
        double progressPercentage = targetMl > 0
                ? Math.min(100.0, (consumedMl * 100.0 / targetMl))
                : 0.0;

        // 判断是否达标
        boolean isAchieved = consumedMl >= targetMl;

        // 构建进度VO
        WaterTargetProgressVO vo = new WaterTargetProgressVO();
        vo.setTargetMl(targetMl);
        vo.setConsumedMl(consumedMl);
        vo.setRemainingMl(remainingMl);
        vo.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0);
        vo.setIsAchieved(isAchieved);
        vo.setQueryDate(formatDate(dateStart)); // 使用标准化的日期
        vo.setIntakeCount((int) intakeCount);

        return vo;
    }

    /**
     * 校验DTO
     */
    private void validateDTO(WaterTargetCreateOrUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了每日目标饮水量，需要校验
        if (dto.getDailyTargetMl() != null) {
            validateDailyTargetMl(dto.getDailyTargetMl());
        }
    }

    /**
     * 校验每日目标饮水量
     */
    private void validateDailyTargetMl(Integer dailyTargetMl) {
        if (dailyTargetMl == null || dailyTargetMl < 500 || dailyTargetMl > 10000) {
            throw new BusinessException("400", "每日目标饮水量必须在500-10000毫升之间");
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
     * 格式化日期为字符串（yyyy-MM-dd格式）
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.toString();
        } catch (Exception e) {
            log.warn("格式化日期失败: {}", date, e);
            return null;
        }
    }

    /**
     * 转换为VO对象
     */
    private WaterTargetVO convertToVO(HealthWaterTargetPO po) {
        WaterTargetVO vo = new WaterTargetVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setDailyTargetMl(po.getDailyTargetMl());
        vo.setReminderEnabled(po.getReminderEnabled() != null && po.getReminderEnabled() == 1);
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());

        // 解析提醒时间间隔
        if (StringUtils.hasText(po.getReminderIntervals())) {
            try {
                List<String> intervals = JSON.parseArray(po.getReminderIntervals(), String.class);
                vo.setReminderIntervals(intervals);
            } catch (Exception e) {
                log.warn("解析提醒时间间隔失败: {}", po.getReminderIntervals(), e);
                vo.setReminderIntervals(null);
            }
        } else {
            vo.setReminderIntervals(null);
        }

        return vo;
    }
}

