package com.pbad.asset.service;

import com.pbad.asset.domain.dto.CurrencyDefaultDTO;
import com.pbad.asset.domain.vo.CurrencyVO;
import com.pbad.asset.domain.vo.ExchangeRateVO;

import java.util.List;

/**
 * 货币服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface CurrencyService {

    /**
     * 获取货币列表
     *
     * @return 货币列表
     */
    List<CurrencyVO> getCurrencyList();

    /**
     * 设置默认货币
     *
     * @param dto 设置默认货币DTO
     */
    void setDefaultCurrency(CurrencyDefaultDTO dto);

    /**
     * 获取默认货币
     *
     * @return 默认货币代码
     */
    String getDefaultCurrency();

    /**
     * 获取汇率
     *
     * @param from 源货币代码
     * @param to   目标货币代码
     * @return 汇率信息
     */
    ExchangeRateVO getExchangeRate(String from, String to);
}

