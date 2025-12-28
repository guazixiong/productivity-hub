package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.WaterStatisticsQueryDTO;
import com.pbad.health.domain.dto.WaterTrendQueryDTO;
import com.pbad.health.domain.vo.*;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.service.HealthWaterStatisticsService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 饮水数据统计服务实现类.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWaterStatisticsServiceImpl implements HealthWaterStatisticsService {

    private final HealthWaterIntakeMapper waterIntakeMapper;
    private final HealthWaterTargetMapper waterTargetMapper;

    // 默认每日目标饮水量（毫升）
    private static final int DEFAULT_DAILY_TARGET_ML = 2000;

    @Override
    @Transactional(readOnly = true)
    public WaterStatisticsVO getStatistics(WaterStatisticsQueryDTO queryDTO, String userId) {
        // 计算日期范围
        Date[] dateRange = calculateDateRange(queryDTO.getPeriod(), queryDTO.getStartDate(), queryDTO.getEndDate());
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // 查询总体统计数据
        Map<String, Object> totalStats = waterIntakeMapper.statisticsByDateRange(userId, startDate, endDate);
        if (totalStats == null) {
            totalStats = new HashMap<>();
        }

        // 构建统计VO
        WaterStatisticsVO vo = new WaterStatisticsVO();
        vo.setTotalIntakeMl(getIntValue(totalStats, "totalIntakeMl", 0));
        vo.setTotalCount(getIntValue(totalStats, "totalCount", 0));

        // 计算平均值
        int totalCount = vo.getTotalCount();
        if (totalCount > 0) {
            vo.setAverageIntakeMl(vo.getTotalIntakeMl() / totalCount);
        } else {
            vo.setAverageIntakeMl(0);
        }

        // 计算达标天数
        int achievementDays = calculateAchievementDays(userId, startDate, endDate);
        vo.setAchievementDays(achievementDays);

        // 按类型分组统计
        List<Map<String, Object>> typeStatsList = waterIntakeMapper.statisticsByType(userId, startDate, endDate);
        if (typeStatsList != null && !typeStatsList.isEmpty()) {
            List<WaterTypeStatisticsVO> typeStatistics = typeStatsList.stream()
                    .map(this::convertToTypeStatisticsVO)
                    .collect(Collectors.toList());
            vo.setTypeStatistics(typeStatistics);
        }

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public WaterTrendVO getTrend(WaterTrendQueryDTO queryDTO, String userId) {
        // 校验天数
        Integer days = queryDTO.getDays();
        if (days == null || (days != 7 && days != 30 && days != 90)) {
            days = 7;
        }

        // 计算日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 查询趋势数据
        List<Map<String, Object>> trendDataList = waterIntakeMapper.queryTrendData(userId, start, end);

        // 查询用户目标配置
        HealthWaterTargetPO targetPo = waterTargetMapper.selectByUserId(userId);
        int targetMl = targetPo != null ? targetPo.getDailyTargetMl() : DEFAULT_DAILY_TARGET_ML;

        // 构建完整的日期序列（填充缺失的日期）
        Map<String, Integer> dataMap = new HashMap<>();
        if (trendDataList != null) {
            for (Map<String, Object> item : trendDataList) {
                String date = (String) item.get("date");
                Object valueObj = item.get("totalVolume");
                int value = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;
                dataMap.put(date, value);
            }
        }

        // 生成完整的日期序列
        List<WaterTrendDataVO> data = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            Integer totalIntakeMl = dataMap.getOrDefault(dateStr, 0);
            
            // 计算达标百分比
            double achievementPercent = targetMl > 0
                    ? Math.min(100.0, (totalIntakeMl * 100.0 / targetMl))
                    : 0.0;
            
            // 判断是否达标
            boolean isAchieved = totalIntakeMl >= targetMl;

            WaterTrendDataVO dataVO = new WaterTrendDataVO();
            dataVO.setDate(dateStr);
            dataVO.setTotalIntakeMl(totalIntakeMl);
            dataVO.setAchievementPercent(Math.round(achievementPercent * 100.0) / 100.0);
            dataVO.setIsAchieved(isAchieved);
            data.add(dataVO);
            
            currentDate = currentDate.plusDays(1);
        }

        // 构建趋势VO
        WaterTrendVO vo = new WaterTrendVO();
        vo.setDays(days);
        vo.setData(data);

        return vo;
    }

    /**
     * 计算日期范围
     */
    private Date[] calculateDateRange(String period, String startDateStr, String endDateStr) {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        if ("custom".equals(period)) {
            // 自定义周期
            if (!StringUtils.hasText(startDateStr) || !StringUtils.hasText(endDateStr)) {
                throw new BusinessException("400", "自定义周期必须提供开始日期和结束日期");
            }
            try {
                startDate = LocalDate.parse(startDateStr.trim());
                endDate = LocalDate.parse(endDateStr.trim());
            } catch (Exception e) {
                throw new BusinessException("400", "日期格式错误，应为yyyy-MM-dd");
            }
            if (endDate.isBefore(startDate)) {
                throw new BusinessException("400", "结束日期不能早于开始日期");
            }
        } else if ("today".equals(period)) {
            // 今天
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        } else if ("week".equals(period)) {
            // 本周（最近7天）
            startDate = LocalDate.now().minusDays(6);
        } else if ("month".equals(period)) {
            // 本月（最近30天）
            startDate = LocalDate.now().minusDays(29);
        } else if ("all".equals(period)) {
            // 全部（不限制日期）
            startDate = null;
            endDate = null;
        } else {
            // 默认今天
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        }

        Date start = startDate != null ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date end = endDate != null ? Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        return new Date[]{start, end};
    }

    /**
     * 计算达标天数
     */
    private int calculateAchievementDays(String userId, Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            // 如果日期范围为空，无法计算达标天数
            return 0;
        }

        // 查询用户目标配置
        HealthWaterTargetPO targetPo = waterTargetMapper.selectByUserId(userId);
        int targetMl = targetPo != null ? targetPo.getDailyTargetMl() : DEFAULT_DAILY_TARGET_ML;

        // 查询趋势数据（按日期分组）
        List<Map<String, Object>> trendDataList = waterIntakeMapper.queryTrendData(userId, startDate, endDate);

        int achievementDays = 0;
        if (trendDataList != null) {
            for (Map<String, Object> item : trendDataList) {
                Object valueObj = item.get("totalVolume");
                int totalIntakeMl = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;
                if (totalIntakeMl >= targetMl) {
                    achievementDays++;
                }
            }
        }

        return achievementDays;
    }

    /**
     * 转换为类型统计VO
     */
    private WaterTypeStatisticsVO convertToTypeStatisticsVO(Map<String, Object> map) {
        WaterTypeStatisticsVO vo = new WaterTypeStatisticsVO();
        vo.setWaterType((String) map.get("waterType"));
        vo.setCount(getIntValue(map, "count", 0));
        vo.setTotalIntakeMl(getIntValue(map, "totalVolume", 0));
        return vo;
    }

    /**
     * 从Map中获取Integer值
     */
    private Integer getIntValue(Map<String, Object> map, String key, Integer defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

