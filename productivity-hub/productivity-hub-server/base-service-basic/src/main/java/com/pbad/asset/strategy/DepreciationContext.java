package com.pbad.asset.strategy;

import com.pbad.asset.domain.po.AssetPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 资产贬值计算策略上下文.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class DepreciationContext {
    
    private final UsageCountDepreciationStrategy usageCountStrategy;
    private final UsageDateDepreciationStrategy usageDateStrategy;
    
    private Map<String, DepreciationStrategy> strategyMap;
    
    /**
     * 初始化策略映射
     */
    private void initStrategyMap() {
        if (strategyMap == null) {
            strategyMap = new HashMap<>();
            strategyMap.put("BY_USAGE_COUNT", usageCountStrategy);
            strategyMap.put("BY_USAGE_DATE", usageDateStrategy);
        }
    }
    
    /**
     * 计算日均价格
     * 
     * @param asset 资产对象
     * @param additionalFeesTotal 附加费用总和
     * @return 日均价格
     */
    public BigDecimal calculate(AssetPO asset, BigDecimal additionalFeesTotal) {
        initStrategyMap();
        
        // 根据资产配置选择策略
        String strategyType = determineStrategyType(asset);
        DepreciationStrategy strategy = strategyMap.get(strategyType);
        
        if (strategy == null) {
            // 默认使用按使用日期计算
            strategy = usageDateStrategy;
        }
        
        return strategy.calculateDailyAverage(asset, additionalFeesTotal);
    }
    
    /**
     * 确定策略类型
     * 
     * @param asset 资产对象
     * @return 策略类型
     */
    private String determineStrategyType(AssetPO asset) {
        // 如果设置了按使用次数计算，优先使用
        if (asset.getDepreciationByUsageCount() != null && asset.getDepreciationByUsageCount()) {
            return "BY_USAGE_COUNT";
        }
        
        // 如果设置了按使用日期计算，使用按使用日期计算
        if (asset.getDepreciationByUsageDate() != null && asset.getDepreciationByUsageDate()) {
            return "BY_USAGE_DATE";
        }
        
        // 默认使用按使用日期计算
        return "BY_USAGE_DATE";
    }
}

