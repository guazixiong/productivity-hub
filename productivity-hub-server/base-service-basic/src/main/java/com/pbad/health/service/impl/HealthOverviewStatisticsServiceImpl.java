package com.pbad.health.service.impl;

import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.domain.po.HealthWeightRecordPO;
import com.pbad.health.domain.vo.HealthCalendarVO;
import com.pbad.health.domain.vo.HealthOverviewVO;
import com.pbad.health.mapper.HealthExerciseRecordMapper;
import com.pbad.health.mapper.HealthUserBodyInfoMapper;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import com.pbad.health.mapper.HealthWeightRecordMapper;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.service.HealthOverviewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 综合统计服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthOverviewStatisticsServiceImpl implements HealthOverviewStatisticsService {

    private final HealthExerciseRecordMapper exerciseRecordMapper;
    private final HealthWaterIntakeMapper waterIntakeMapper;
    private final HealthWaterTargetMapper waterTargetMapper;
    private final HealthWeightRecordMapper weightRecordMapper;
    private final HealthUserBodyInfoMapper userBodyInfoMapper;

    // 默认每日目标饮水量（毫升）
    private static final int DEFAULT_DAILY_TARGET_ML = 2000;

    @Override
    @Transactional(readOnly = true)
    public HealthOverviewVO getOverview(String period, String userId) {
        if (period == null || period.isEmpty()) {
            period = "today";
        }

        HealthOverviewVO vo = new HealthOverviewVO();

        // 计算日期范围
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        LocalDate monthStart = today.minusDays(29);

        // 运动概览
        vo.setExercise(buildExerciseOverview(userId, today, weekStart, monthStart));

        // 饮水概览
        vo.setWater(buildWaterOverview(userId, today, weekStart, monthStart));

        // 体重概览
        vo.setWeight(buildWeightOverview(userId, today, weekStart, monthStart));

        // 数据完成度
        vo.setDataCompleteness(buildDataCompleteness(userId, monthStart, today));

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthCalendarVO getCalendar(Integer year, Integer month, String userId) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }

        // 校验月份范围
        if (month < 1 || month > 12) {
            month = LocalDate.now().getMonthValue();
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 查询有记录的日期
        Set<String> exerciseDates = exerciseRecordMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());
        Set<String> waterDates = waterIntakeMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());
        Set<String> weightDates = weightRecordMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());

        // 构建日历数据
        List<HealthCalendarVO.CalendarDay> days = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            HealthCalendarVO.CalendarDay day = new HealthCalendarVO.CalendarDay();
            day.setDate(dateStr);
            day.setHasExercise(exerciseDates.contains(dateStr));
            day.setHasWater(waterDates.contains(dateStr));
            day.setHasWeight(weightDates.contains(dateStr));
            days.add(day);
            currentDate = currentDate.plusDays(1);
        }

        HealthCalendarVO vo = new HealthCalendarVO();
        vo.setYear(year);
        vo.setMonth(month);
        vo.setDays(days);

        return vo;
    }

    /**
     * 构建运动概览
     */
    private HealthOverviewVO.ExerciseOverview buildExerciseOverview(String userId, LocalDate today, LocalDate weekStart, LocalDate monthStart) {
        HealthOverviewVO.ExerciseOverview overview = new HealthOverviewVO.ExerciseOverview();

        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date weekStartDate = Date.from(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date weekEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date monthStartDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date monthEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 今日统计
        Map<String, Object> todayStats = exerciseRecordMapper.statisticsByDateRange(userId, todayStart, todayEnd);
        overview.setTodayDuration(getIntValue(todayStats, "totalDuration", 0));
        overview.setTodayCount(getIntValue(todayStats, "totalCount", 0));
        overview.setTodayCalories(getIntValue(todayStats, "totalCalories", 0));

        // 本周统计
        Map<String, Object> weekStats = exerciseRecordMapper.statisticsByDateRange(userId, weekStartDate, weekEnd);
        overview.setWeekDuration(getIntValue(weekStats, "totalDuration", 0));
        overview.setWeekCount(getIntValue(weekStats, "totalCount", 0));
        overview.setWeekCalories(getIntValue(weekStats, "totalCalories", 0));

        // 本月统计
        Map<String, Object> monthStats = exerciseRecordMapper.statisticsByDateRange(userId, monthStartDate, monthEnd);
        overview.setMonthDuration(getIntValue(monthStats, "totalDuration", 0));
        overview.setMonthCount(getIntValue(monthStats, "totalCount", 0));
        overview.setMonthCalories(getIntValue(monthStats, "totalCalories", 0));

        return overview;
    }

    /**
     * 构建饮水概览
     */
    private HealthOverviewVO.WaterOverview buildWaterOverview(String userId, LocalDate today, LocalDate weekStart, LocalDate monthStart) {
        HealthOverviewVO.WaterOverview overview = new HealthOverviewVO.WaterOverview();

        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date weekStartDate = Date.from(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date weekEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date monthStartDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date monthEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 获取目标饮水量
        HealthWaterTargetPO target = waterTargetMapper.selectByUserId(userId);
        int targetMl = target != null && target.getDailyTargetMl() != null
                ? target.getDailyTargetMl()
                : DEFAULT_DAILY_TARGET_ML;

        // 今日统计
        Integer todayIntake = waterIntakeMapper.sumVolumeByDate(userId, todayStart);
        todayIntake = todayIntake != null ? todayIntake : 0;
        overview.setTodayIntakeMl(todayIntake);
        overview.setTodayTargetMl(targetMl);
        overview.setTodayProgress(todayIntake > 0 && targetMl > 0
                ? (double) todayIntake / targetMl * 100.0
                : 0.0);
        overview.setTodayIsAchieved(todayIntake >= targetMl);

        // 本周统计
        Map<String, Object> weekStats = waterIntakeMapper.statisticsByDateRange(userId, weekStartDate, weekEnd);
        overview.setWeekIntakeMl(getIntValue(weekStats, "totalIntakeMl", 0));
        overview.setWeekAchievementDays(getIntValue(weekStats, "achievementDays", 0));

        // 本月统计
        Map<String, Object> monthStats = waterIntakeMapper.statisticsByDateRange(userId, monthStartDate, monthEnd);
        overview.setMonthIntakeMl(getIntValue(monthStats, "totalIntakeMl", 0));
        overview.setMonthAchievementDays(getIntValue(monthStats, "achievementDays", 0));

        return overview;
    }

    /**
     * 构建体重概览
     */
    private HealthOverviewVO.WeightOverview buildWeightOverview(String userId, LocalDate today, LocalDate weekStart, LocalDate monthStart) {
        HealthOverviewVO.WeightOverview overview = new HealthOverviewVO.WeightOverview();

        Date todayEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date weekStartDate = Date.from(weekStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date weekEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        Date monthStartDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date monthEnd = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 获取用户身体信息（目标体重）
        HealthUserBodyInfoPO bodyInfo = userBodyInfoMapper.selectByUserId(userId);
        BigDecimal targetWeight = bodyInfo != null && bodyInfo.getTargetWeightKg() != null
                ? bodyInfo.getTargetWeightKg()
                : null;
        overview.setTargetWeight(targetWeight);

        // 最新体重
        HealthWeightRecordPO latestRecord = weightRecordMapper.selectLatestByDateRange(userId, null, todayEnd);
        BigDecimal latestWeight = latestRecord != null ? latestRecord.getWeightKg() : null;
        overview.setLatestWeight(latestWeight);

        // 与目标体重的差距
        if (latestWeight != null && targetWeight != null) {
            overview.setGapFromTarget(latestWeight.subtract(targetWeight));
        } else {
            overview.setGapFromTarget(null);
        }

        // 本周变化
        HealthWeightRecordPO weekStartRecord = weightRecordMapper.selectLatestByDateRange(userId, weekStartDate, weekStartDate);
        if (latestRecord != null && weekStartRecord != null) {
            overview.setWeekChange(latestRecord.getWeightKg().subtract(weekStartRecord.getWeightKg()));
        } else {
            overview.setWeekChange(null);
        }

        // 本月变化
        HealthWeightRecordPO monthStartRecord = weightRecordMapper.selectLatestByDateRange(userId, monthStartDate, monthStartDate);
        if (latestRecord != null && monthStartRecord != null) {
            overview.setMonthChange(latestRecord.getWeightKg().subtract(monthStartRecord.getWeightKg()));
        } else {
            overview.setMonthChange(null);
        }

        return overview;
    }

    /**
     * 构建数据完成度
     */
    private HealthOverviewVO.DataCompleteness buildDataCompleteness(String userId, LocalDate startDate, LocalDate endDate) {
        HealthOverviewVO.DataCompleteness completeness = new HealthOverviewVO.DataCompleteness();

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 查询有记录的日期数
        Set<String> exerciseDates = exerciseRecordMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());
        Set<String> waterDates = waterIntakeMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());
        Set<String> weightDates = weightRecordMapper.queryDatesWithRecords(userId, start, end)
                .stream().map(d -> formatDate(d)).collect(Collectors.toSet());

        completeness.setExerciseDays(exerciseDates.size());
        completeness.setWaterDays(waterDates.size());
        completeness.setWeightDays(weightDates.size());

        // 计算总天数
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        completeness.setTotalDays((int) totalDays);

        // 计算完成度百分比（有任意一种记录的日期数 / 总天数）
        Set<String> allDates = new HashSet<>();
        allDates.addAll(exerciseDates);
        allDates.addAll(waterDates);
        allDates.addAll(weightDates);
        double completenessPercent = totalDays > 0
                ? (double) allDates.size() / totalDays * 100.0
                : 0.0;
        completeness.setCompletenessPercent(BigDecimal.valueOf(completenessPercent)
                .setScale(1, RoundingMode.HALF_UP).doubleValue());

        return completeness;
    }

    /**
     * 从Map中获取整数值
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * 格式化日期为yyyy-MM-dd
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }
}

