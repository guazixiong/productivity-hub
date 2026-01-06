package com.pbad.asset.observer;

/**
 * 资产事件监听器接口.
 *
 * 使用观察者模式解耦资产业务逻辑与后续通知、统计等处理逻辑。
 */
public interface AssetEventListener {

    /**
     * 处理资产事件.
     *
     * @param event 资产事件
     */
    void onAssetEvent(AssetEvent event);
}


