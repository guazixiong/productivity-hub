package com.pbad.asset.service;

import com.pbad.asset.domain.dto.AssetSoldCreateDTO;
import com.pbad.asset.domain.dto.AssetSoldUpdateDTO;
import com.pbad.asset.domain.vo.AssetSoldVO;

/**
 * 资产卖出服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetSoldService {

    /**
     * 根据资产ID获取卖出记录
     *
     * @param assetId 资产ID
     * @return 卖出记录
     */
    AssetSoldVO getSoldByAssetId(String assetId);

    /**
     * 创建卖出记录
     *
     * @param createDTO 创建DTO
     * @return 创建的卖出记录
     */
    AssetSoldVO createSold(AssetSoldCreateDTO createDTO);

    /**
     * 更新卖出记录
     *
     * @param updateDTO 更新DTO
     * @return 更新后的卖出记录
     */
    AssetSoldVO updateSold(AssetSoldUpdateDTO updateDTO);
}

