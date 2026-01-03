package com.pbad.asset.statistics.factory.impl;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.vo.StatisticsTrendVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.statistics.factory.ChartDataGenerator;
import com.pbad.asset.statistics.factory.ChartType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产总额趋势图表数据生成器.
 *
 * <p>生成按日期统计的资产总额趋势数据，用于折线图展示。</p>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class TotalValueTrendGenerator implements ChartDataGenerator<StatisticsTrendVO> {

    private final AssetMapper assetMapper;
    private final AssetAdditionalFeeMapper additionalFeeMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ChartType getType() {
        return ChartType.TOTAL_VALUE_TREND;
    }

    @Override
    public StatisticsTrendVO generate(String userId, StatisticsQueryDTO query) {
        // 计算日期范围
        Date[] dateRange = calculateDateRange(userId, query);
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        // 查询用户的所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);

        // 过滤在日期范围内的资产（根据购买日期）
        List<AssetPO> filteredAssets = assets.stream()
                .filter(asset -> {
                    if (asset.getPurchaseDate() == null) {
                        return false;
                    }
                    if (startDate != null && asset.getPurchaseDate().before(startDate)) {
                        return false;
                    }
                    if (endDate != null && asset.getPurchaseDate().after(endDate)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 预计算每个资产的总价值
        Map<String, BigDecimal> assetTotalValueMap = new HashMap<>();
        for (AssetPO asset : filteredAssets) {
            BigDecimal totalValue = asset.getPrice() != null ? asset.getPrice() : BigDecimal.ZERO;
            
            // 累加附加费用
            List<AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            for (AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    totalValue = totalValue.add(fee.getAmount());
                }
            }
            
            assetTotalValueMap.put(asset.getId(), totalValue);
        }

        // 按日期分组，计算每天的资产总额
        Map<String, BigDecimal> dailyTotalValueMap = new TreeMap<>();
        
        // 生成日期列表
        List<String> dates = generateDateList(startDate, endDate);
        for (String date : dates) {
            dailyTotalValueMap.put(date, BigDecimal.ZERO);
        }

        // 计算每个资产在每天贡献的总价值
        for (AssetPO asset : filteredAssets) {
            BigDecimal totalValue = assetTotalValueMap.get(asset.getId());
            
            // 计算资产在日期范围内的每一天
            LocalDate purchaseDate = convertToLocalDate(asset.getPurchaseDate());
            LocalDate assetEndDate = calculateAssetEndDate(asset);
            
            LocalDate currentDate = purchaseDate;
            LocalDate queryStartDate = startDate != null ? convertToLocalDate(startDate) : purchaseDate;
            LocalDate queryEndDate = endDate != null ? convertToLocalDate(endDate) : assetEndDate;
            
            // 从查询开始日期或购买日期中较晚的开始
            if (currentDate.isBefore(queryStartDate)) {
                currentDate = queryStartDate;
            }
            
            // 到查询结束日期或资产结束日期中较早的结束
            LocalDate effectiveEndDate = assetEndDate.isBefore(queryEndDate) ? assetEndDate : queryEndDate;
            
            while (!currentDate.isAfter(effectiveEndDate)) {
                String dateStr = currentDate.format(DATE_FORMATTER);
                dailyTotalValueMap.put(dateStr, dailyTotalValueMap.get(dateStr).add(totalValue));
                currentDate = currentDate.plusDays(1);
            }
        }

        // 构建返回结果
        StatisticsTrendVO vo = new StatisticsTrendVO();
        vo.setDates(new ArrayList<>(dailyTotalValueMap.keySet()));
        vo.setValues(new ArrayList<>(dailyTotalValueMap.values()));
        
        return vo;
    }

    /**
     * 计算日期范围
     */
    private Date[] calculateDateRange(String userId, StatisticsQueryDTO query) {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();

        String period = query.getPeriod() != null ? query.getPeriod().toUpperCase() : "WEEK";
        
        if (StringUtils.hasText(query.getStartDate()) && StringUtils.hasText(query.getEndDate())) {
            // 自定义日期范围
            try {
                startDate = LocalDate.parse(query.getStartDate().trim());
                endDate = LocalDate.parse(query.getEndDate().trim());
            } catch (Exception e) {
                throw new RuntimeException("日期格式错误，应为yyyy-MM-dd", e);
            }
            if (endDate.isBefore(startDate)) {
                throw new RuntimeException("结束日期不能早于开始日期");
            }
        } else if ("WEEK".equals(period)) {
            // 本周（最近7天）
            startDate = LocalDate.now().minusDays(6);
        } else if ("MONTH".equals(period)) {
            // 本月（最近30天）
            startDate = LocalDate.now().minusDays(29);
        } else if ("YEAR".equals(period)) {
            // 本年（最近365天）
            startDate = LocalDate.now().minusDays(364);
        } else if ("ALL".equals(period)) {
            // 全部：查询所有资产的最早购买日期作为开始日期
            List<AssetPO> allAssets = assetMapper.selectAllByUserId(userId);
            if (allAssets.isEmpty()) {
                startDate = LocalDate.now();
            } else {
                Date earliestDate = allAssets.stream()
                        .filter(asset -> asset.getPurchaseDate() != null)
                        .map(AssetPO::getPurchaseDate)
                        .min(Date::compareTo)
                        .orElse(new Date());
                startDate = convertToLocalDate(earliestDate);
            }
        } else {
            // 默认本周
            startDate = LocalDate.now().minusDays(6);
        }

        Date start = startDate != null ? Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
        Date end = endDate != null ? Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;

        return new Date[]{start, end};
    }

    /**
     * 生成日期列表
     */
    private List<String> generateDateList(Date startDate, Date endDate) {
        List<String> dates = new ArrayList<>();
        if (startDate == null || endDate == null) {
            return dates;
        }
        
        LocalDate start = convertToLocalDate(startDate);
        LocalDate end = convertToLocalDate(endDate);
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current.format(DATE_FORMATTER));
            current = current.plusDays(1);
        }
        
        return dates;
    }

    /**
     * 计算资产的结束日期
     */
    private LocalDate calculateAssetEndDate(AssetPO asset) {
        if ("RETIRED".equals(asset.getStatus()) && asset.getRetiredDate() != null) {
            return convertToLocalDate(asset.getRetiredDate());
        }
        // 使用当前日期
        return LocalDate.now();
    }

    /**
     * 转换Date为LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

