package com.pbad.basic.tools.controller;

import com.pbad.tools.domain.dto.LdxpGoodsQueryDTO;
import com.pbad.tools.domain.vo.CursorCommodityVO;
import com.pbad.tools.domain.vo.LdxpGoodsVO;
import com.pbad.tools.service.CursorShopService;
import com.pbad.tools.service.LdxpShopService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cursor 邮箱自助小店相关接口.
 */
@RestController
@RequestMapping("/api/tools/cursor-shop")
@RequiredArgsConstructor
public class CursorShopController {

    private final CursorShopService cursorShopService;
    private final LdxpShopService ldxpShopService;

    /**
     * 获取库存商品信息列表（实时拉取）.
     */
    @GetMapping("/commodities")
    public ApiResponse<List<CursorCommodityVO>> getCommodities() {
        List<CursorCommodityVO> commodities = cursorShopService.fetchCommodities();
        return ApiResponse.ok(commodities);
    }

    /**
     * 获取链动小铺库存列表（后端代理调用）.
     */
    @PostMapping("/ldxp/goods")
    public ApiResponse<List<LdxpGoodsVO>> getLdxpGoods(@RequestBody(required = false) LdxpGoodsQueryDTO query) {
        List<LdxpGoodsVO> goods = ldxpShopService.fetchGoods(query);
        return ApiResponse.ok(goods);
    }
}

