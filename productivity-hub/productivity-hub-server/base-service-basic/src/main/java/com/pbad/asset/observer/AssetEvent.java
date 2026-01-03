package com.pbad.asset.observer;

import com.pbad.asset.domain.enums.AssetEventType;
import com.pbad.asset.domain.po.AssetPO;

import java.time.LocalDateTime;

/**
 * 资产事件.
 *
 * 封装资产状态变更时的关键信息，供各观察者使用。
 */
public class AssetEvent {

    /**
     * 事件类型
     */
    private final AssetEventType eventType;

    /**
     * 相关资产对象
     */
    private final AssetPO asset;

    /**
     * 事件发生时间
     */
    private final LocalDateTime timestamp;

    public AssetEvent(AssetEventType eventType, AssetPO asset) {
        this.eventType = eventType;
        this.asset = asset;
        this.timestamp = LocalDateTime.now();
    }

    public AssetEventType getEventType() {
        return eventType;
    }

    public AssetPO getAsset() {
        return asset;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}


