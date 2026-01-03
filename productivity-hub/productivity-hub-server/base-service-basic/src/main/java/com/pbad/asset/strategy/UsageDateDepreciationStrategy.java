package com.pbad.asset.strategy;

import com.pbad.asset.domain.po.AssetPO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 按使用日期计算贬值策略.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
public class UsageDateDepreciationStrategy implements DepreciationStrategy {
    
    @Override
    public BigDecimal calculateDailyAverage(AssetPO asset, BigDecimal additionalFeesTotal) {
        // 计算资产总价值
        BigDecimal totalValue = asset.getPrice();
        if (additionalFeesTotal != null) {
            totalValue = totalValue.add(additionalFeesTotal);
        }
        
        // 获取购买日期
        Date purchaseDate = asset.getPurchaseDate();
        if (purchaseDate == null) {
            return totalValue; // 如果没有购买日期，返回总价值
        }
        
        // 计算使用天数
        LocalDate purchaseLocalDate = convertToLocalDate(purchaseDate);
        LocalDate endDate = calculateEndDate(asset);
        long usageDays = ChronoUnit.DAYS.between(purchaseLocalDate, endDate);
        
        // 当天购买时，使用天数记为1天
        if (usageDays <= 0) {
            usageDays = 1;
        }
        
        // 计算日均价格：总价值 / 使用天数
        return totalValue.divide(BigDecimal.valueOf(usageDays), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyType() {
        return "BY_USAGE_DATE";
    }
    
    /**
     * 计算结束日期
     */
    private LocalDate calculateEndDate(AssetPO asset) {
        // 如果资产已退役，使用退役日期
        if ("RETIRED".equals(asset.getStatus()) && asset.getRetiredDate() != null) {
            return convertToLocalDate(asset.getRetiredDate());
        }
        
        // 如果资产已卖出，使用卖出日期（卖出日期在AssetSoldPO中，这里简化处理）
        if ("SOLD".equals(asset.getStatus())) {
            // 卖出日期需要从AssetSoldPO中获取，这里使用当前日期
            return LocalDate.now();
        }
        
        // 否则使用系统当前日期
        return LocalDate.now();
    }
    
    /**
     * Date转LocalDate
     */
    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}

