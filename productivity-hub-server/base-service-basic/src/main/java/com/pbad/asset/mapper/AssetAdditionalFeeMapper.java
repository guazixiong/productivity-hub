package com.pbad.asset.mapper;

import com.pbad.asset.domain.po.AssetAdditionalFeePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 附加费用 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetAdditionalFeeMapper {

    /**
     * 根据资产ID查询附加费用列表
     *
     * @param assetId 资产ID
     * @return 附加费用列表
     */
    List<AssetAdditionalFeePO> selectByAssetId(@Param("assetId") String assetId);

    /**
     * 根据ID查询附加费用
     *
     * @param id 附加费用ID
     * @return 附加费用
     */
    AssetAdditionalFeePO selectById(@Param("id") String id);

    /**
     * 插入附加费用
     *
     * @param fee 附加费用
     * @return 插入行数
     */
    int insertFee(AssetAdditionalFeePO fee);

    /**
     * 更新附加费用
     *
     * @param fee 附加费用
     * @return 更新行数
     */
    int updateFee(AssetAdditionalFeePO fee);

    /**
     * 删除附加费用
     *
     * @param id 附加费用ID
     * @return 删除行数
     */
    int deleteFee(@Param("id") String id);
}

