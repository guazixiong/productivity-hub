package com.pbad.asset.state;

import com.pbad.asset.domain.po.AssetPO;
import org.springframework.stereotype.Component;

/**
 * 已退役状态.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
public class RetiredState implements AssetState {
    
    @Override
    public boolean canEdit() {
        return true;
    }
    
    @Override
    public boolean canRetire() {
        return false;
    }
    
    @Override
    public boolean canSell() {
        return true;
    }
    
    @Override
    public void retire(AssetPO asset) {
        // 已退役状态不能再退役
        throw new IllegalStateException("资产已退役，不能再次退役");
    }
    
    @Override
    public void sell(AssetPO asset) {
        asset.setStatus("SOLD");
        asset.setInService(false);
        // 卖出操作由AssetSoldService处理
    }
}

