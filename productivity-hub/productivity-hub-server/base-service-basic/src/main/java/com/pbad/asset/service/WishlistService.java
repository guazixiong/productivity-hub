package com.pbad.asset.service;

import com.pbad.asset.domain.dto.WishlistCreateDTO;
import com.pbad.asset.domain.dto.WishlistUpdateDTO;
import com.pbad.asset.domain.vo.WishlistVO;

import java.util.List;

/**
 * 心愿单服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface WishlistService {

    /**
     * 获取心愿单列表
     *
     * @param achieved 是否已实现（可选）
     * @return 心愿单列表
     */
    List<WishlistVO> getWishlistList(Boolean achieved);

    /**
     * 根据ID获取心愿单
     *
     * @param id 心愿单ID
     * @return 心愿单
     */
    WishlistVO getWishlistById(String id);

    /**
     * 创建心愿单
     *
     * @param createDTO 创建DTO
     * @return 创建的心愿单
     */
    WishlistVO createWishlist(WishlistCreateDTO createDTO);

    /**
     * 更新心愿单
     *
     * @param updateDTO 更新DTO
     * @return 更新后的心愿单
     */
    WishlistVO updateWishlist(WishlistUpdateDTO updateDTO);

    /**
     * 删除心愿单
     *
     * @param id 心愿单ID
     */
    void deleteWishlist(String id);
}

