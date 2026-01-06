package com.pbad.asset.strategy;

import com.pbad.asset.domain.po.AssetPO;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产贬值计算策略接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface DepreciationStrategy {
    /**
     * 计算日均价格
     * 
     * @param asset 资产对象
     * @param additionalFeesTotal 附加费用总和
     * @return 日均价格
     */
    BigDecimal calculateDailyAverage(AssetPO asset, BigDecimal additionalFeesTotal);
    
    /**
     * 获取策略类型
     * 
     * @return 策略类型
     */
    String getStrategyType();
}

