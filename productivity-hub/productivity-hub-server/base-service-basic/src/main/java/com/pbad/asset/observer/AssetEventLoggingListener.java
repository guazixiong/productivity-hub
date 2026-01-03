package com.pbad.asset.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 资产事件日志监听器.
 *
 * 作为观察者模式的基础示例，实现简单的日志记录，
 * 便于后续扩展为消息通知、审计日志等。
 */
@Slf4j
@Component
public class AssetEventLoggingListener implements AssetEventListener {

    @Override
    public void onAssetEvent(AssetEvent event) {
        if (event == null || event.getAsset() == null) {
            return;
        }
        log.info("Asset event received: type={}, assetId={}, status={}, timestamp={}",
                event.getEventType(),
                event.getAsset().getId(),
                event.getAsset().getStatus(),
                event.getTimestamp());
    }
}


