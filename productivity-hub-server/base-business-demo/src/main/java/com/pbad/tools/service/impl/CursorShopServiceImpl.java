package com.pbad.tools.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.tools.domain.vo.CursorCommodityVO;
import com.pbad.tools.service.CursorShopService;
import common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Cursor 邮箱自助小店服务实现.
 */
@Slf4j
@Service
public class CursorShopServiceImpl implements CursorShopService {

    private static final String COMMODITY_URL = "https://tuhjk.asia/user/api/index/commodity?categoryId=3";
    private static final String COMMODITY_URL_HTTP = "http://tuhjk.asia/user/api/index/commodity?categoryId=3";

    private final RestTemplate restTemplate;

    public CursorShopServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        // 配置短超时与 UA，提升被目标站点接受的概率
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .additionalInterceptors(userAgentInterceptor())
                .build();
    }

    @Override
    public List<CursorCommodityVO> fetchCommodities() {
        try {
            String response = fetchWithFallback();
            if (!StringUtils.hasText(response)) {
                throw new BusinessException("500", "接口返回为空");
            }
            JSONObject root = JSON.parseObject(response);
            Integer code = root.getInteger("code");
            if (code == null || code != 200) {
                throw new BusinessException("500", "接口返回异常，code=" + code + ", msg=" + root.getString("msg"));
            }
            JSONArray data = root.getJSONArray("data");
            if (data == null) {
                return new ArrayList<>();
            }

            List<CursorCommodityVO> result = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                Integer stockState = item.getInteger("stock_state");
                result.add(CursorCommodityVO.builder()
                        .id(item.getInteger("id"))
                        .name(item.getString("name"))
                        .price(item.getBigDecimal("price"))
                        .stock(item.getInteger("stock"))
                        .orderSold(item.getInteger("order_sold"))
                        .stockState(stockState)
                        .stockStateText(stockStateText(stockState))
                        .build());
            }
            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("拉取商品信息失败: {}", ex.getMessage(), ex);
            throw new BusinessException("500", "拉取商品信息失败: " + ex.getMessage());
        }
    }

    private String fetchWithFallback() {
        try {
            return restTemplate.getForObject(COMMODITY_URL, String.class);
        } catch (ResourceAccessException ex) {
            log.warn("HTTPS 请求失败，尝试 HTTP 回退: {}", ex.getMessage());
            return restTemplate.getForObject(COMMODITY_URL_HTTP, String.class);
        }
    }

    private ClientHttpRequestInterceptor userAgentInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().set("User-Agent", "productivity-hub-cursor-shop/1.0");
            return execution.execute(request, body);
        };
    }

    private String stockStateText(Integer stockState) {
        if (stockState == null) {
            return "-";
        }
        switch (stockState) {
            case 0:
                return "缺货";
            case 1:
                return "紧张";
            case 2:
                return "充足";
            default:
                return String.valueOf(stockState);
        }
    }
}

