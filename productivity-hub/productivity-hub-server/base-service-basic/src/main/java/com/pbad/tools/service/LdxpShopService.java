package com.pbad.tools.service;

import com.pbad.tools.domain.dto.LdxpGoodsQueryDTO;
import com.pbad.tools.domain.vo.LdxpGoodsVO;

import java.util.List;

/**
 * 链动小铺服务.
 */
public interface LdxpShopService {

    /**
     * 拉取商品列表（实时库存信息）.
     *
     * @param query 查询参数，可为空
     * @return 商品列表
     */
    List<LdxpGoodsVO> fetchGoods(LdxpGoodsQueryDTO query);
}

