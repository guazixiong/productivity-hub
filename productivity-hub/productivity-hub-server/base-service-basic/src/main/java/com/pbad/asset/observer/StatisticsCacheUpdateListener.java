package com.pbad.asset.observer;

import com.pbad.asset.service.AssetStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 统计缓存更新监听器.
 *
 * 当前实现为简单调用统计服务的清理缓存方法，便于后续替换为真实缓存实现。
 */
@Component
@RequiredArgsConstructor
public class StatisticsCacheUpdateListener implements AssetEventListener {

    private final AssetStatisticsService statisticsService;

    @Override
    public void onAssetEvent(AssetEvent event) {
        // 任何资产事件发生后，清理统计相关缓存
        statisticsService.clearCache();
    }
}


