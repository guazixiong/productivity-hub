package com.pbad.asset.strategy;

import com.pbad.asset.domain.po.AssetPO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 按使用次数计算贬值策略.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
public class UsageCountDepreciationStrategy implements DepreciationStrategy {
    
    @Override
    public BigDecimal calculateDailyAverage(AssetPO asset, BigDecimal additionalFeesTotal) {
        // 计算资产总价值
        BigDecimal totalValue = asset.getPrice();
        if (additionalFeesTotal != null) {
            totalValue = totalValue.add(additionalFeesTotal);
        }
        
        // 获取预计使用次数
        Integer expectedUsageCount = asset.getExpectedUsageCount();
        if (expectedUsageCount == null || expectedUsageCount <= 0) {
            return totalValue; // 如果没有设置使用次数，返回总价值
        }
        
        // 获取已使用次数（这里简化处理，实际应从使用记录中获取）
        Integer usedCount = getUsedCount(asset);
        
        // 计算剩余使用次数
        int remainingCount = expectedUsageCount - usedCount;
        if (remainingCount <= 0) {
            return totalValue; // 如果已用完，返回总价值
        }
        
        // 计算日均价格：总价值 / 剩余使用次数
        return totalValue.divide(BigDecimal.valueOf(remainingCount), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getStrategyType() {
        return "BY_USAGE_COUNT";
    }
    
    /**
     * 获取已使用次数（简化处理，实际应从数据库查询使用记录）
     */
    private Integer getUsedCount(AssetPO asset) {
        // TODO: 实际应从使用记录表中查询
        return 0;
    }
}

