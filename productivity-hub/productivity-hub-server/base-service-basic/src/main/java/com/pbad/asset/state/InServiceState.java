package com.pbad.asset.state;

import com.pbad.asset.domain.po.AssetPO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 正在服役状态.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
public class InServiceState implements AssetState {
    
    @Override
    public boolean canEdit() {
        return true;
    }
    
    @Override
    public boolean canRetire() {
        return true;
    }
    
    @Override
    public boolean canSell() {
        return true;
    }
    
    @Override
    public void retire(AssetPO asset) {
        asset.setStatus("RETIRED");
        asset.setInService(false);
        // 设置退役日期
        asset.setRetiredDate(new Date());
    }
    
    @Override
    public void sell(AssetPO asset) {
        asset.setStatus("SOLD");
        asset.setInService(false);
        // 卖出操作由AssetSoldService处理
    }
}

