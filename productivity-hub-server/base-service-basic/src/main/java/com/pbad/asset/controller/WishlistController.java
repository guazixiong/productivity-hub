package com.pbad.asset.controller;

import com.pbad.asset.domain.dto.WishlistCreateDTO;
import com.pbad.asset.domain.dto.WishlistUpdateDTO;
import com.pbad.asset.domain.vo.WishlistVO;
import com.pbad.asset.service.WishlistService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心愿单控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 获取心愿单列表
     */
    @GetMapping
    public ApiResponse<List<WishlistVO>> getWishlistList(@RequestParam(required = false) Boolean achieved) {
        List<WishlistVO> wishlists = wishlistService.getWishlistList(achieved);
        return ApiResponse.ok(wishlists);
    }

    /**
     * 根据ID获取心愿单
     */
    @GetMapping("/{id}")
    public ApiResponse<WishlistVO> getWishlistById(@PathVariable String id) {
        WishlistVO wishlist = wishlistService.getWishlistById(id);
        return ApiResponse.ok(wishlist);
    }

    /**
     * 创建心愿单
     */
    @PostMapping
    public ApiResponse<WishlistVO> createWishlist(@RequestBody WishlistCreateDTO createDTO) {
        WishlistVO wishlist = wishlistService.createWishlist(createDTO);
        return ApiResponse.ok(wishlist);
    }

    /**
     * 更新心愿单
     */
    @PutMapping
    public ApiResponse<WishlistVO> updateWishlist(@RequestBody WishlistUpdateDTO updateDTO) {
        WishlistVO wishlist = wishlistService.updateWishlist(updateDTO);
        return ApiResponse.ok(wishlist);
    }

    /**
     * 删除心愿单
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWishlist(@PathVariable String id) {
        wishlistService.deleteWishlist(id);
        return ApiResponse.ok(null);
    }
}

