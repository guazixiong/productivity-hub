package com.pbad.asset.controller;

import com.pbad.asset.domain.dto.CurrencyDefaultDTO;
import com.pbad.asset.domain.vo.CurrencyVO;
import com.pbad.asset.domain.vo.ExchangeRateVO;
import com.pbad.asset.service.CurrencyService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 货币控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    /**
     * 获取货币列表
     */
    @GetMapping("/list")
    public ApiResponse<List<CurrencyVO>> getCurrencyList() {
        List<CurrencyVO> currencies = currencyService.getCurrencyList();
        return ApiResponse.ok(currencies);
    }

    /**
     * 设置默认货币
     */
    @PostMapping("/default")
    public ApiResponse<Void> setDefaultCurrency(@RequestBody CurrencyDefaultDTO dto) {
        currencyService.setDefaultCurrency(dto);
        return ApiResponse.ok(null);
    }

    /**
     * 获取默认货币
     */
    @GetMapping("/default")
    public ApiResponse<String> getDefaultCurrency() {
        String currencyCode = currencyService.getDefaultCurrency();
        return ApiResponse.ok(currencyCode);
    }

    /**
     * 获取汇率
     */
    @GetMapping("/exchange-rate")
    public ApiResponse<ExchangeRateVO> getExchangeRate(
            @RequestParam String from,
            @RequestParam String to) {
        ExchangeRateVO rate = currencyService.getExchangeRate(from, to);
        return ApiResponse.ok(rate);
    }
}

