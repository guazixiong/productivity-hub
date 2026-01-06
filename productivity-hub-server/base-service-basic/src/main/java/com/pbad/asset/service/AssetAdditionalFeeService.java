package com.pbad.asset.service;

import com.pbad.asset.domain.dto.AssetAdditionalFeeCreateDTO;
import com.pbad.asset.domain.dto.AssetAdditionalFeeUpdateDTO;
import com.pbad.asset.domain.vo.AssetAdditionalFeeVO;

import java.util.List;

/**
 * 附加费用服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetAdditionalFeeService {

    /**
     * 根据资产ID获取附加费用列表
     *
     * @param assetId 资产ID
     * @return 附加费用列表
     */
    List<AssetAdditionalFeeVO> getFeesByAssetId(String assetId);

    /**
     * 创建附加费用
     *
     * @param createDTO 创建DTO
     * @return 创建的附加费用
     */
    AssetAdditionalFeeVO createFee(AssetAdditionalFeeCreateDTO createDTO);

    /**
     * 更新附加费用
     *
     * @param updateDTO 更新DTO
     * @return 更新后的附加费用
     */
    AssetAdditionalFeeVO updateFee(AssetAdditionalFeeUpdateDTO updateDTO);

    /**
     * 删除附加费用
     *
     * @param id 附加费用ID
     */
    void deleteFee(String id);
}

