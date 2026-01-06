package com.pbad.asset.statistics.factory;

import com.pbad.asset.domain.dto.StatisticsQueryDTO;
import common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 图表数据生成器工厂.
 *
 * <p>在应用启动时收集所有 {@link ChartDataGenerator} 实现，</p>
 * <p>并根据 {@link ChartType} 提供统一的获取入口。</p>
 */
@Component
public class ChartDataGeneratorFactory {

    private final Map<ChartType, ChartDataGenerator<?>> generatorMap;

    /**
     * 通过构造函数收集所有生成器并构建按类型索引的 Map.
     *
     * @param generators Spring 注入的所有生成器实现
     */
    public ChartDataGeneratorFactory(List<ChartDataGenerator<?>> generators) {
        this.generatorMap = new EnumMap<>(ChartType.class);
        for (ChartDataGenerator<?> generator : generators) {
            this.generatorMap.put(generator.getType(), generator);
        }
    }

    /**
     * 获取指定类型的图表数据生成器.
     *
     * @param type 图表类型
     * @param <T>  图表数据类型
     * @return 对应的生成器
     */
    @SuppressWarnings("unchecked")
    public <T> ChartDataGenerator<T> getGenerator(ChartType type) {
        ChartDataGenerator<?> generator = generatorMap.get(type);
        if (generator == null) {
            throw new BusinessException("40090", "不支持的统计图表类型: " + type);
        }
        return (ChartDataGenerator<T>) generator;
    }

    /**
     * 直接生成指定类型的图表数据的便捷方法.
     *
     * @param type   图表类型
     * @param userId 用户ID
     * @param query  查询条件
     * @param <T>    图表数据类型
     * @return 图表数据
     */
    public <T> T generate(ChartType type, String userId, StatisticsQueryDTO query) {
        ChartDataGenerator<T> generator = getGenerator(type);
        return generator.generate(userId, query);
    }
}


