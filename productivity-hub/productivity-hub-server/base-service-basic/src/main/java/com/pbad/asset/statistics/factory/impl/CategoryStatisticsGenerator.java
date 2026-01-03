package com.pbad.asset.statistics.factory.impl;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.domain.po.AssetPO;
import com.pbad.asset.domain.vo.CategoryStatisticsVO;
import com.pbad.asset.mapper.AssetAdditionalFeeMapper;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import com.pbad.asset.statistics.factory.ChartDataGenerator;
import com.pbad.asset.statistics.factory.ChartType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分类统计图表数据生成器.
 *
 * <p>生成按分类统计的资产数量和总价值数据，用于柱状图展示。</p>
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class CategoryStatisticsGenerator implements ChartDataGenerator<List<CategoryStatisticsVO>> {

    private final AssetMapper assetMapper;
    private final AssetCategoryMapper categoryMapper;
    private final AssetAdditionalFeeMapper additionalFeeMapper;

    @Override
    public ChartType getType() {
        return ChartType.CATEGORY_STATISTICS;
    }

    @Override
    public List<CategoryStatisticsVO> generate(String userId, StatisticsQueryDTO query) {
        // 查询用户的所有资产
        List<AssetPO> assets = assetMapper.selectAllByUserId(userId);
        
        // 查询所有分类
        List<AssetCategoryPO> categories = categoryMapper.selectAll();
        Map<String, String> categoryIdToNameMap = categories.stream()
                .collect(Collectors.toMap(AssetCategoryPO::getId, AssetCategoryPO::getName));

        // 按分类分组统计
        Map<String, CategoryStatisticsVO> categoryStatsMap = new HashMap<>();

        for (AssetPO asset : assets) {
            String categoryId = asset.getCategoryId();
            String categoryName = categoryIdToNameMap.getOrDefault(categoryId, "未分类");

            // 获取或创建分类统计对象
            CategoryStatisticsVO stats = categoryStatsMap.computeIfAbsent(categoryName, 
                    k -> {
                        CategoryStatisticsVO vo = new CategoryStatisticsVO();
                        vo.setCategoryName(k);
                        vo.setCount(0);
                        vo.setTotalValue(BigDecimal.ZERO);
                        return vo;
                    });

            // 增加资产数量
            stats.setCount(stats.getCount() + 1);

            // 计算资产总价值
            BigDecimal assetValue = asset.getPrice() != null ? asset.getPrice() : BigDecimal.ZERO;
            
            // 累加附加费用
            List<AssetAdditionalFeePO> fees = additionalFeeMapper.selectByAssetId(asset.getId());
            for (AssetAdditionalFeePO fee : fees) {
                if (fee.getAmount() != null) {
                    assetValue = assetValue.add(fee.getAmount());
                }
            }

            // 累加总价值
            stats.setTotalValue(stats.getTotalValue().add(assetValue));
        }

        // 构建返回结果
        List<CategoryStatisticsVO> result = new ArrayList<>(categoryStatsMap.values());

        // 按总价值降序排序
        result.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));

        return result;
    }
}

