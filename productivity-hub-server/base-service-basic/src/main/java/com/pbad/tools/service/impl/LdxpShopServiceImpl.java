package com.pbad.tools.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.tools.domain.dto.LdxpGoodsQueryDTO;
import com.pbad.tools.domain.vo.LdxpGoodsVO;
import com.pbad.tools.service.LdxpShopService;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 链动小铺服务实现.
 */
@Slf4j
@Service
public class LdxpShopServiceImpl implements LdxpShopService {

    private static final String GOODS_URL = "https://pay.ldxp.cn/shopApi/Shop/goodsList";

    private final RestTemplate restTemplate;

    public LdxpShopServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .additionalInterceptors(userAgentInterceptor())
                .build();
    }

    @Override
    public List<LdxpGoodsVO> fetchGoods(LdxpGoodsQueryDTO query) {
        Map<String, Object> payload = buildPayload(query);
        try {
            String response = restTemplate.postForObject(GOODS_URL, payload, String.class);
            if (!StringUtils.hasText(response)) {
                throw new BusinessException("500", "链动小铺接口返回为空");
            }
            JSONObject root = JSON.parseObject(response);
            Integer code = root.getInteger("code");
            if (!Objects.equals(code, 1)) {
                throw new BusinessException("500", root.getString("msg"));
            }
            JSONObject data = root.getJSONObject("data");
            JSONArray list = data == null ? null : data.getJSONArray("list");
            if (list == null || list.isEmpty()) {
                return new ArrayList<>();
            }
            List<LdxpGoodsVO> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                result.add(mapItem(item));
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("拉取链动小铺商品失败: {}", ex.getMessage(), ex);
            throw new BusinessException("500", "拉取链动小铺商品失败: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildPayload(LdxpGoodsQueryDTO query) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", "WCT9GRNG");
        payload.put("keywords", "");
        payload.put("category_id", 12585);
        payload.put("goods_type", "card");
        payload.put("current", 1);
        payload.put("pageSize", 20);

        if (query == null) {
            return payload;
        }

        if (StringUtils.hasText(query.getToken())) {
            payload.put("token", query.getToken());
        }
        if (StringUtils.hasText(query.getKeywords())) {
            payload.put("keywords", query.getKeywords());
        }
        if (StringUtils.hasText(query.getGoodsType())) {
            payload.put("goods_type", query.getGoodsType());
        }
        if (query.getCategoryId() != null) {
            payload.put("category_id", query.getCategoryId());
        }
        if (query.getCurrent() != null) {
            payload.put("current", query.getCurrent());
        }
        if (query.getPageSize() != null) {
            payload.put("pageSize", query.getPageSize());
        }

        return payload;
    }

    private LdxpGoodsVO mapItem(JSONObject item) {
        return LdxpGoodsVO.builder()
                .link(item.getString("link"))
                .goodsType(item.getString("goods_type"))
                .goodsKey(item.getString("goods_key"))
                .name(item.getString("name"))
                .price(item.getBigDecimal("price"))
                .marketPrice(item.getBigDecimal("market_price"))
                .description(item.getString("description"))
                .createTime(item.getLong("create_time"))
                .image(item.getString("image"))
                .couponStatus(item.getInteger("coupon_status"))
                .category(mapCategory(item.getJSONObject("category")))
                .user(mapUser(item.getJSONObject("user")))
                .discount(item.get("discount"))
                .multipleoffers(item.get("multipleoffers"))
                .fullgift(item.get("fullgift"))
                .extend(mapExtend(item.getJSONObject("extend")))
                .build();
    }

    private LdxpGoodsVO.LdxpGoodsCategoryVO mapCategory(JSONObject category) {
        if (category == null) {
            return null;
        }
        return LdxpGoodsVO.LdxpGoodsCategoryVO.builder()
                .id(category.getInteger("id"))
                .name(category.getString("name"))
                .build();
    }

    private LdxpGoodsVO.LdxpGoodsUserVO mapUser(JSONObject user) {
        if (user == null) {
            return null;
        }
        return LdxpGoodsVO.LdxpGoodsUserVO.builder()
                .link(user.getString("link"))
                .nickname(user.getString("nickname"))
                .avatar(user.getString("avatar"))
                .token(user.getString("token"))
                .build();
    }

    private LdxpGoodsVO.LdxpGoodsExtendVO mapExtend(JSONObject extend) {
        if (extend == null) {
            return null;
        }
        return LdxpGoodsVO.LdxpGoodsExtendVO.builder()
                .stockCount(extend.getInteger("stock_count"))
                .showStockType(extend.getInteger("show_stock_type"))
                .sendOrder(extend.getInteger("send_order"))
                .limitCount(extend.getInteger("limit_count"))
                .build();
    }

    private ClientHttpRequestInterceptor userAgentInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().set("User-Agent", "productivity-hub-ldxp-shop/1.0");
            return execution.execute(request, body);
        };
    }
}

