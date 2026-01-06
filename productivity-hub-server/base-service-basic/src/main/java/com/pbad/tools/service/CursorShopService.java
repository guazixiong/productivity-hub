package com.pbad.tools.service;

import com.pbad.tools.domain.vo.CursorCommodityVO;

import java.util.List;

/**
 * Cursor 邮箱自助小店服务.
 */
public interface CursorShopService {

    /**
     * 拉取商品列表（实时库存信息）.
     *
     * @return 商品列表
     */
    List<CursorCommodityVO> fetchCommodities();
}

