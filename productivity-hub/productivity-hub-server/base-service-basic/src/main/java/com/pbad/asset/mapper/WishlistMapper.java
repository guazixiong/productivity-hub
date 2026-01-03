package com.pbad.asset.mapper;

import com.pbad.asset.domain.po.WishlistPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 心愿单 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface WishlistMapper {

    /**
     * 查询用户的心愿单列表
     *
     * @param userId   用户ID
     * @param achieved 是否已实现（可选）
     * @return 心愿单列表
     */
    List<WishlistPO> selectAll(@Param("userId") String userId, @Param("achieved") Boolean achieved);

    /**
     * 根据ID查询心愿单
     *
     * @param id     心愿单ID
     * @param userId 用户ID
     * @return 心愿单
     */
    WishlistPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 插入心愿单
     *
     * @param wishlist 心愿单
     * @return 插入行数
     */
    int insertWishlist(WishlistPO wishlist);

    /**
     * 更新心愿单
     *
     * @param wishlist 心愿单
     * @return 更新行数
     */
    int updateWishlist(WishlistPO wishlist);

    /**
     * 删除心愿单
     *
     * @param id     心愿单ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteWishlist(@Param("id") String id, @Param("userId") String userId);
}

