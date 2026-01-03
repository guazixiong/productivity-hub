package com.pbad.asset.observer;

import com.pbad.asset.domain.enums.AssetEventType;
import com.pbad.asset.domain.po.AssetPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 资产事件发布者.
 *
 * 负责在资产业务发生变化时，将事件广播给所有已注册的监听器。
 */
@Slf4j
@Component
public class AssetEventPublisher {

    private final List<AssetEventListener> listeners = new ArrayList<>();

    public AssetEventPublisher(List<AssetEventListener> listeners) {
        if (listeners != null) {
            this.listeners.addAll(listeners);
        }
    }

    /**
     * 手动注册监听器（保留扩展能力）.
     *
     * @param listener 监听器
     */
    public void addListener(AssetEventListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * 发布资产事件.
     *
     * @param eventType 事件类型
     * @param asset     资产对象
     */
    public void publish(AssetEventType eventType, AssetPO asset) {
        if (eventType == null || asset == null) {
            return;
        }
        AssetEvent event = new AssetEvent(eventType, asset);
        for (AssetEventListener listener : listeners) {
            try {
                listener.onAssetEvent(event);
            } catch (Exception e) {
                // 单个监听器异常不影响整体流程
                log.warn("Handle asset event failed in listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
}


