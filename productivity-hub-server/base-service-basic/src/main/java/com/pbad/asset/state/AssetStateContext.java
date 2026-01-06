package com.pbad.asset.state;

import com.pbad.asset.domain.po.AssetPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 资产状态上下文.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class AssetStateContext {
    
    private final InServiceState inServiceState;
    private final RetiredState retiredState;
    private final SoldState soldState;
    
    private Map<String, AssetState> stateMap;
    
    /**
     * 初始化状态映射
     */
    private void initStateMap() {
        if (stateMap == null) {
            stateMap = new HashMap<>();
            stateMap.put("IN_SERVICE", inServiceState);
            stateMap.put("RETIRED", retiredState);
            stateMap.put("SOLD", soldState);
        }
    }
    
    /**
     * 获取资产状态
     * 
     * @param asset 资产对象
     * @return 资产状态
     */
    public AssetState getState(AssetPO asset) {
        initStateMap();
        
        String status = asset.getStatus();
        if (status == null) {
            // 默认状态为正在服役
            return inServiceState;
        }
        
        AssetState state = stateMap.get(status);
        if (state == null) {
            // 默认状态为正在服役
            return inServiceState;
        }
        
        return state;
    }
    
    /**
     * 是否可以编辑
     * 
     * @param asset 资产对象
     * @return true表示可以编辑，false表示不可以编辑
     */
    public boolean canEdit(AssetPO asset) {
        return getState(asset).canEdit();
    }
    
    /**
     * 是否可以退役
     * 
     * @param asset 资产对象
     * @return true表示可以退役，false表示不可以退役
     */
    public boolean canRetire(AssetPO asset) {
        return getState(asset).canRetire();
    }
    
    /**
     * 是否可以卖出
     * 
     * @param asset 资产对象
     * @return true表示可以卖出，false表示不可以卖出
     */
    public boolean canSell(AssetPO asset) {
        return getState(asset).canSell();
    }
    
    /**
     * 执行退役操作
     * 
     * @param asset 资产对象
     */
    public void retire(AssetPO asset) {
        AssetState state = getState(asset);
        if (!state.canRetire()) {
            throw new IllegalStateException("当前状态不允许退役操作");
        }
        state.retire(asset);
    }
    
    /**
     * 执行卖出操作
     * 
     * @param asset 资产对象
     */
    public void sell(AssetPO asset) {
        AssetState state = getState(asset);
        if (!state.canSell()) {
            throw new IllegalStateException("当前状态不允许卖出操作");
        }
        state.sell(asset);
    }
}

