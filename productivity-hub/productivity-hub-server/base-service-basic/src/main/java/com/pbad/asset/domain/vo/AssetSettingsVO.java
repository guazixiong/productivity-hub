package com.pbad.asset.domain.vo;

import lombok.Data;

/**
 * 资产设置视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class AssetSettingsVO {
    /**
     * 退役资产不计入总额
     */
    private Boolean excludeRetired;

    /**
     * 总资产算二手盈利
     */
    private Boolean calculateProfit;
}

