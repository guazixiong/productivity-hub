package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.ExerciseStatisticsQueryDTO;
import com.pbad.health.domain.dto.ExerciseTrendQueryDTO;
import com.pbad.health.domain.vo.*;
import com.pbad.health.mapper.HealthExerciseRecordMapper;
import com.pbad.health.service.HealthExerciseStatisticsService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运动数据统计服务实现类.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthExerciseStatisticsServiceImpl implements HealthExerciseStatisticsService {

    private final HealthExerciseRecordMapper exerciseRecordMapper;

    @Override
    @Transactional(readOnly = true)
    public ExerciseStatisticsVO getStatistics(ExerciseStatisticsQueryDTO queryDTO, String userId) {
        // 计算日期范围
        Date[] dateRange = calculateDateRange(queryDTO.getPeriod(), queryDTO.getStartDate(), queryDTO.getEndDate());
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // 查询总体统计数据
        Map<String, Object> totalStats = exerciseRecordMapper.statisticsByDateRange(userId, startDate, endDate);
        if (totalStats == null) {
            totalStats = new HashMap<>();
        }

        // 构建统计VO
        ExerciseStatisticsVO vo = new ExerciseStatisticsVO();
        vo.setTotalDuration(getIntValue(totalStats, "totalDuration", 0));
        vo.setTotalCount(getIntValue(totalStats, "totalCount", 0));
        vo.setTotalCalories(getIntValue(totalStats, "totalCalories", 0));
        vo.setTotalDistance(getBigDecimalValue(totalStats, "totalDistance", BigDecimal.ZERO));

        // 计算平均值
        int totalCount = vo.getTotalCount();
        if (totalCount > 0) {
            vo.setAverageDuration(vo.getTotalDuration() / totalCount);
            vo.setAverageCalories(vo.getTotalCalories() / totalCount);
        } else {
            vo.setAverageDuration(0);
            vo.setAverageCalories(0);
        }

        // 按分组维度查询
        String groupBy = queryDTO.getGroupBy();
        if ("type".equals(groupBy)) {
            // 按类型分组统计
            List<Map<String, Object>> typeStatsList = exerciseRecordMapper.statisticsByType(userId, startDate, endDate);
            if (typeStatsList != null && !typeStatsList.isEmpty()) {
                List<ExerciseTypeStatisticsVO> typeStatistics = typeStatsList.stream()
                        .map(this::convertToTypeStatisticsVO)
                        .collect(Collectors.toList());
                vo.setTypeStatistics(typeStatistics);
            }
        } else if ("plan".equals(groupBy)) {
            // 按计划分组统计
            List<Map<String, Object>> planStatsList = exerciseRecordMapper.statisticsByPlan(userId, startDate, endDate);
            if (planStatsList != null && !planStatsList.isEmpty()) {
                List<ExercisePlanStatisticsVO> planStatistics = planStatsList.stream()
                        .map(this::convertToPlanStatisticsVO)
                        .collect(Collectors.toList());
                vo.setPlanStatistics(planStatistics);
            }
        }

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseTrendVO getTrend(ExerciseTrendQueryDTO queryDTO, String userId) {
        // 校验趋势类型
        String type = queryDTO.getType();
        if (!StringUtils.hasText(type)) {
            type = "duration";
        }
        if (!"duration".equals(type) && !"count".equals(type) && !"calories".equals(type)) {
            type = "duration";
        }

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
        List<Map<String, Object>> trendDataList = exerciseRecordMapper.queryTrendData(userId, start, end, type);

        // 构建完整的日期序列（填充缺失的日期）
        Map<String, Integer> dataMap = new HashMap<>();
        if (trendDataList != null) {
            for (Map<String, Object> item : trendDataList) {
                String date = (String) item.get("date");
                Object valueObj = item.get("value");
                int value = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;
                dataMap.put(date, value);
            }
        }

        // 生成完整的日期序列
        List<ExerciseTrendDataVO> data = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            ExerciseTrendDataVO dataVO = new ExerciseTrendDataVO();
            dataVO.setDate(dateStr);
            dataVO.setValue(dataMap.getOrDefault(dateStr, 0));
            data.add(dataVO);
            currentDate = currentDate.plusDays(1);
        }

        // 构建趋势VO
        ExerciseTrendVO vo = new ExerciseTrendVO();
        vo.setType(type);
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
     * 转换为类型统计VO
     */
    private ExerciseTypeStatisticsVO convertToTypeStatisticsVO(Map<String, Object> map) {
        ExerciseTypeStatisticsVO vo = new ExerciseTypeStatisticsVO();
        vo.setExerciseType((String) map.get("exerciseType"));
        vo.setCount(getIntValue(map, "count", 0));
        vo.setTotalDuration(getIntValue(map, "totalDuration", 0));
        vo.setTotalCalories(getIntValue(map, "totalCalories", 0));
        vo.setTotalDistance(getBigDecimalValue(map, "totalDistance", BigDecimal.ZERO));
        return vo;
    }

    /**
     * 转换为计划统计VO
     */
    private ExercisePlanStatisticsVO convertToPlanStatisticsVO(Map<String, Object> map) {
        ExercisePlanStatisticsVO vo = new ExercisePlanStatisticsVO();
        vo.setPlanId((String) map.get("planId"));
        vo.setPlanName((String) map.get("planName"));
        vo.setCount(getIntValue(map, "count", 0));
        vo.setTotalDuration(getIntValue(map, "totalDuration", 0));
        vo.setTotalCalories(getIntValue(map, "totalCalories", 0));
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

    /**
     * 从Map中获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key, BigDecimal defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

