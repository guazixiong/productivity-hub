package com.pbad.health.service.impl;

import com.pbad.health.domain.dto.WeightStatisticsQueryDTO;
import com.pbad.health.domain.dto.WeightTrendQueryDTO;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.domain.po.HealthWeightRecordPO;
import com.pbad.health.domain.vo.WeightStatisticsVO;
import com.pbad.health.domain.vo.WeightTrendDataVO;
import com.pbad.health.domain.vo.WeightTrendVO;
import com.pbad.health.mapper.HealthUserBodyInfoMapper;
import com.pbad.health.mapper.HealthWeightRecordMapper;
import com.pbad.health.service.HealthWeightStatisticsService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * 体重数据统计服务实现类.
 * 关联需求：REQ-HEALTH-002
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWeightStatisticsServiceImpl implements HealthWeightStatisticsService {

    private final HealthWeightRecordMapper weightRecordMapper;
    private final HealthUserBodyInfoMapper userBodyInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public WeightStatisticsVO getStatistics(WeightStatisticsQueryDTO queryDTO, String userId) {
        // 计算日期范围
        Date[] dateRange = calculateDateRange(queryDTO.getPeriod(), queryDTO.getStartDate(), queryDTO.getEndDate());
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // 查询总体统计数据
        Map<String, Object> totalStats = weightRecordMapper.statisticsByDateRange(userId, startDate, endDate);
        if (totalStats == null) {
            totalStats = new HashMap<>();
        }

        // 查询最新和最早的体重记录
        HealthWeightRecordPO latestRecord = weightRecordMapper.selectLatestByDateRange(userId, startDate, endDate);
        HealthWeightRecordPO earliestRecord = weightRecordMapper.selectEarliestByDateRange(userId, startDate, endDate);

        // 查询用户身体信息（获取目标体重）
        HealthUserBodyInfoPO bodyInfo = userBodyInfoMapper.selectByUserId(userId);

        // 构建统计VO
        WeightStatisticsVO vo = new WeightStatisticsVO();

        // 最新体重
        if (latestRecord != null) {
            vo.setLatestWeight(latestRecord.getWeightKg());
        } else {
            vo.setLatestWeight(BigDecimal.ZERO);
        }

        // 目标体重
        BigDecimal targetWeight = bodyInfo != null && bodyInfo.getTargetWeightKg() != null
                ? bodyInfo.getTargetWeightKg()
                : null;
        vo.setTargetWeight(targetWeight);

        // 与目标体重的差距
        if (vo.getLatestWeight() != null && targetWeight != null) {
            vo.setGapFromTarget(vo.getLatestWeight().subtract(targetWeight));
        } else {
            vo.setGapFromTarget(null);
        }

        // 平均体重
        vo.setAverageWeight(getBigDecimalValue(totalStats, "averageWeight", BigDecimal.ZERO));

        // 最高体重
        vo.setMaxWeight(getBigDecimalValue(totalStats, "maxWeight", BigDecimal.ZERO));

        // 最低体重
        vo.setMinWeight(getBigDecimalValue(totalStats, "minWeight", BigDecimal.ZERO));

        // 记录数
        vo.setRecordCount(getIntValue(totalStats, "recordCount", 0));

        // 体重变化（相对于周期开始时的体重）
        if (latestRecord != null && earliestRecord != null) {
            BigDecimal weightChange = latestRecord.getWeightKg().subtract(earliestRecord.getWeightKg());
            vo.setWeightChange(weightChange);

            // 体重变化率（%）
            if (earliestRecord.getWeightKg().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal changeRate = weightChange
                        .divide(earliestRecord.getWeightKg(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                vo.setWeightChangeRate(changeRate);
            } else {
                vo.setWeightChangeRate(BigDecimal.ZERO);
            }
        } else {
            vo.setWeightChange(BigDecimal.ZERO);
            vo.setWeightChangeRate(BigDecimal.ZERO);
        }

        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public WeightTrendVO getTrend(WeightTrendQueryDTO queryDTO, String userId) {
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
        List<Map<String, Object>> trendDataList = weightRecordMapper.queryTrendData(userId, start, end);

        // 查询用户身体信息（获取目标体重）
        HealthUserBodyInfoPO bodyInfo = userBodyInfoMapper.selectByUserId(userId);
        BigDecimal targetWeight = bodyInfo != null && bodyInfo.getTargetWeightKg() != null
                ? bodyInfo.getTargetWeightKg()
                : null;

        // 构建完整的日期序列（填充缺失的日期）
        Map<String, Map<String, Object>> dataMap = new HashMap<>();
        if (trendDataList != null) {
            for (Map<String, Object> item : trendDataList) {
                String date = (String) item.get("date");
                dataMap.put(date, item);
            }
        }

        // 生成完整的日期序列
        List<WeightTrendDataVO> data = new ArrayList<>();
        BigDecimal previousWeight = null;
        LocalDate currentDate = startDate;
        List<BigDecimal> weightList = new ArrayList<>();

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.toString();
            Map<String, Object> dayData = dataMap.get(dateStr);

            WeightTrendDataVO dataVO = new WeightTrendDataVO();
            dataVO.setDate(dateStr);

            if (dayData != null) {
                BigDecimal weightKg = getBigDecimalValue(dayData, "weightKg", BigDecimal.ZERO);
                dataVO.setWeightKg(weightKg);
                dataVO.setBmi(getBigDecimalValue(dayData, "bmi", BigDecimal.ZERO));
                dataVO.setBodyFatPercentage(null); // queryTrendData中没有返回体脂率，设为null

                // 计算与前一天相比的变化
                if (previousWeight != null) {
                    dataVO.setChangeFromPrevious(weightKg.subtract(previousWeight));
                } else {
                    dataVO.setChangeFromPrevious(BigDecimal.ZERO);
                }

                previousWeight = weightKg;
                if (weightKg.compareTo(BigDecimal.ZERO) > 0) {
                    weightList.add(weightKg);
                }
            } else {
                // 没有数据，使用前一天的值或0
                dataVO.setWeightKg(previousWeight != null ? previousWeight : BigDecimal.ZERO);
                dataVO.setBmi(BigDecimal.ZERO);
                dataVO.setBodyFatPercentage(null);
                dataVO.setChangeFromPrevious(BigDecimal.ZERO);
            }

            data.add(dataVO);
            currentDate = currentDate.plusDays(1);
        }

        // 构建趋势VO
        WeightTrendVO vo = new WeightTrendVO();
        vo.setDays(days);
        vo.setData(data);

        // 计算汇总数据
        if (!weightList.isEmpty()) {
            // 最新体重（最后一条有数据的记录）
            BigDecimal latestWeight = weightList.get(weightList.size() - 1);
            vo.setLatestWeight(latestWeight);

            // 目标体重
            vo.setTargetWeight(targetWeight);

            // 与目标体重的差距
            if (targetWeight != null) {
                vo.setGapFromTarget(latestWeight.subtract(targetWeight));
            } else {
                vo.setGapFromTarget(null);
            }

            // 平均体重
            BigDecimal sum = weightList.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal averageWeight = sum.divide(new BigDecimal(weightList.size()), 2, RoundingMode.HALF_UP);
            vo.setAverageWeight(averageWeight);

            // 最高体重
            vo.setMaxWeight(weightList.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

            // 最低体重
            vo.setMinWeight(weightList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        } else {
            vo.setLatestWeight(BigDecimal.ZERO);
            vo.setTargetWeight(targetWeight);
            vo.setGapFromTarget(null);
            vo.setAverageWeight(BigDecimal.ZERO);
            vo.setMaxWeight(BigDecimal.ZERO);
            vo.setMinWeight(BigDecimal.ZERO);
        }

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
        } else if ("week".equals(period)) {
            // 本周（最近7天）
            startDate = LocalDate.now().minusDays(6);
        } else if ("month".equals(period)) {
            // 本月（最近30天）
            startDate = LocalDate.now().minusDays(29);
        } else if ("quarter".equals(period)) {
            // 本季度（最近90天）
            startDate = LocalDate.now().minusDays(89);
        } else {
            // 默认本周
            startDate = LocalDate.now().minusDays(6);
        }

        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return new Date[]{start, end};
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

