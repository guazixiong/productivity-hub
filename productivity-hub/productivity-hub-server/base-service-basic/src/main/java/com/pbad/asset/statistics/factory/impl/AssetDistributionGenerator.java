package com.pbad.asset.statistics.factory.impl;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.vo.AssetDistributionVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.statistics.factory.ChartDataGenerator;
import com.pbad.asset.statistics.factory.ChartType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 资产分布图表数据生成器.
 *
 * <p>生成按分类统计的资产分布数据，用于饼图展示。</p>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class AssetDistributionGenerator implements ChartDataGenerator<List<AssetDistributionVO>> {

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper categoryMapper;
    private final AssetAdditionalFeeMapper additionalFeeMapper;

    @Override
    public ChartType getType() {
        return ChartType.ASSET_DISTRIBUTION;
    }

    @Override
    public List<AssetDistributionVO> generate(String userId, StatisticsQueryDTO query) {
        // 查询用户的所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);
        
        // 查询所有分类
        List<AssetCategoryPO> categories = categoryMapper.selectAll();
        Map<String, String> categoryIdToNameMap = categories.stream()
                .collect(Collectors.toMap(AssetCategoryPO::getId, AssetCategoryPO::getName));

        // 按分类分组计算总价值
        Map<String, BigDecimal> categoryValueMap = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;

        for (AssetPO asset : assets) {
            String categoryId = asset.getCategoryId();
            String categoryName = categoryIdToNameMap.getOrDefault(categoryId, "未分类");

            // 计算资产总价值
            BigDecimal assetValue = asset.getPrice() != null ? asset.getPrice() : BigDecimal.ZERO;
            
            // 累加附加费用
            List<AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            for (AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    assetValue = assetValue.add(fee.getAmount());
                }
            }

            // 累加到分类总价值
            categoryValueMap.put(categoryName, 
                    categoryValueMap.getOrDefault(categoryName, BigDecimal.ZERO).add(assetValue));
            totalValue = totalValue.add(assetValue);
        }

        // 构建返回结果
        List<AssetDistributionVO> result = new ArrayList<>();
        
        for (Map.Entry<String, BigDecimal> entry : categoryValueMap.entrySet()) {
            AssetDistributionVO vo = new AssetDistributionVO();
            vo.setCategoryName(entry.getKey());
            vo.setValue(entry.getValue());
            
            // 计算百分比
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = entry.getValue()
                        .divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                vo.setPercentage(percentage);
            } else {
                vo.setPercentage(BigDecimal.ZERO);
            }
            
            result.add(vo);
        }

        // 按价值降序排序
        result.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        return result;
    }
}

