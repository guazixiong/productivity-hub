package com.pbad.asset.service;

import com.pbad.asset.domain.dto.*;
import com.pbad.asset.domain.vo.AssetDetailVO;
import com.pbad.asset.domain.vo.AssetListVO;
import com.pbad.asset.domain.vo.AssetPageVO;

import java.util.List;

/**
 * 资产服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetService {

    /**
     * 分页查询资产列表
     *
     * @param queryDTO 查询DTO
     * @return 资产分页数据
     */
    AssetPageVO getAssetPage(AssetQueryDTO queryDTO);

    /**
     * 根据ID获取资产详情
     *
     * @param id 资产ID
     * @return 资产详情
     */
    AssetDetailVO getAssetById(String id);

    /**
     * 创建资产
     *
     * @param createDTO 创建DTO
     * @return 创建的资产
     */
    AssetDetailVO createAsset(AssetCreateDTO createDTO);

    /**
     * 更新资产
     *
     * @param updateDTO 更新DTO
     * @return 更新后的资产
     */
    AssetDetailVO updateAsset(AssetUpdateDTO updateDTO);

    /**
     * 删除资产
     *
     * @param id 资产ID
     */
    void deleteAsset(String id);

    /**
     * 批量删除资产
     *
     * @param ids 资产ID列表
     */
    void batchDeleteAssets(List<String> ids);

    /**
     * 更新资产状态
     *
     * @param updateStatusDTO 状态更新DTO
     */
    void updateAssetStatus(AssetStatusUpdateDTO updateStatusDTO);
}

