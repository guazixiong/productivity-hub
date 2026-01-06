package com.pbad.asset.mapper;

import com.pbad.asset.domain.po.AssetPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetMapper {

    /**
     * 分页查询资产列表
     *
     * @param userId     用户ID
     * @param categoryId 分类ID（可选）
     * @param status     状态（可选）
     * @param offset     偏移量
     * @param pageSize   每页数量
     * @return 资产列表
     */
    List<AssetPO> selectPage(@Param("userId") String userId,
                            @Param("categoryId") String categoryId,
                            @Param("status") String status,
                            @Param("offset") int offset,
                            @Param("pageSize") int pageSize);

    /**
     * 统计资产总数
     *
     * @param userId     用户ID
     * @param categoryId 分类ID（可选）
     * @param status     状态（可选）
     * @return 总数
     */
    long count(@Param("userId") String userId,
               @Param("categoryId") String categoryId,
               @Param("status") String status);

    /**
     * 根据ID查询资产
     *
     * @param id     资产ID
     * @param userId 用户ID
     * @return 资产
     */
    AssetPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 插入资产
     *
     * @param asset 资产
     * @return 插入行数
     */
    int insertAsset(AssetPO asset);

    /**
     * 更新资产
     *
     * @param asset 资产
     * @return 更新行数
     */
    int updateAsset(AssetPO asset);

    /**
     * 软删除资产
     *
     * @param id     资产ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteAsset(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量软删除资产
     *
     * @param ids    资产ID列表
     * @param userId 用户ID
     * @return 删除行数
     */
    int batchDeleteAssets(@Param("ids") List<String> ids, @Param("userId") String userId);

    /**
     * 查询用户的所有资产（用于统计）
     *
     * @param userId 用户ID
     * @return 资产列表
     */
    List<AssetPO> selectAllByUserId(@Param("userId") String userId);
}

