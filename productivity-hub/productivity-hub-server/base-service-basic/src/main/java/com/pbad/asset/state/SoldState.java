package com.pbad.asset.state;

import com.pbad.asset.domain.po.AssetPO;
import org.springframework.stereotype.Component;

/**
 * 已卖出状态.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
public class SoldState implements AssetState {
    
    @Override
    public boolean canEdit() {
        return false;
    }
    
    @Override
    public boolean canRetire() {
        return false;
    }
    
    @Override
    public boolean canSell() {
        return false;
    }
    
    @Override
    public void retire(AssetPO asset) {
        // 已卖出状态不能退役
        throw new IllegalStateException("资产已卖出，不能退役");
    }
    
    @Override
    public void sell(AssetPO asset) {
        // 已卖出状态不能再卖出
        throw new IllegalStateException("资产已卖出，不能再次卖出");
    }
}

