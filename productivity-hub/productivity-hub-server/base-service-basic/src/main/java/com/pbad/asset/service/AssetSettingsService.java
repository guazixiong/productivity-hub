package com.pbad.asset.service;

import com.pbad.asset.domain.dto.AssetSettingsUpdateDTO;
import com.pbad.asset.domain.vo.AssetSettingsVO;

/**
 * 资产设置服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetSettingsService {

    /**
     * 获取资产设置
     *
     * @return 资产设置
     */
    AssetSettingsVO getAssetSettings();

    /**
     * 更新资产设置
     *
     * @param dto 更新DTO
     * @return 更新后的资产设置
     */
    AssetSettingsVO updateAssetSettings(AssetSettingsUpdateDTO dto);
}

