package com.pbad.tools.controller;

import com.pbad.tools.domain.vo.CursorCommodityVO;
import com.pbad.tools.service.CursorShopService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 获取库存商品信息列表（实时拉取）.
     */
    @GetMapping("/commodities")
    public ApiResponse<List<CursorCommodityVO>> getCommodities() {
        List<CursorCommodityVO> commodities = cursorShopService.fetchCommodities();
        return ApiResponse.ok(commodities);
    }
}

