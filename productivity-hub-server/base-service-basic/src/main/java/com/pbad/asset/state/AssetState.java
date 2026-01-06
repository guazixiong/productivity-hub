package com.pbad.asset.state;

import com.pbad.asset.domain.po.AssetPO;

/**
 * 资产状态接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetState {
    /**
     * 是否可以编辑
     * 
     * @return true表示可以编辑，false表示不可以编辑
     */
    boolean canEdit();
    
    /**
     * 是否可以退役
     * 
     * @return true表示可以退役，false表示不可以退役
     */
    boolean canRetire();
    
    /**
     * 是否可以卖出
     * 
     * @return true表示可以卖出，false表示不可以卖出
     */
    boolean canSell();
    
    /**
     * 执行退役操作
     * 
     * @param asset 资产对象
     */
    void retire(AssetPO asset);
    
    /**
     * 执行卖出操作
     * 
     * @param asset 资产对象
     */
    void sell(AssetPO asset);
}

