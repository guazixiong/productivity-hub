package com.pbad.asset.mapper;

import com.pbad.asset.domain.po.AssetSoldPO;
import org.apache.ibatis.annotations.Param;

/**
 * 资产卖出 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetSoldMapper {

    /**
     * 根据资产ID查询卖出记录
     *
     * @param assetId 资产ID
     * @return 卖出记录
     */
    AssetSoldPO selectByAssetId(@Param("assetId") String assetId);

    /**
     * 根据ID查询卖出记录
     *
     * @param id 卖出记录ID
     * @return 卖出记录
     */
    AssetSoldPO selectById(@Param("id") String id);

    /**
     * 插入卖出记录
     *
     * @param sold 卖出记录
     * @return 插入行数
     */
    int insertSold(AssetSoldPO sold);

    /**
     * 更新卖出记录
     *
     * @param sold 卖出记录
     * @return 更新行数
     */
    int updateSold(AssetSoldPO sold);
}

