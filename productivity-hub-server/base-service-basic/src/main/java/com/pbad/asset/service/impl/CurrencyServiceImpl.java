package com.pbad.asset.service.impl;

import com.pbad.asset.decorator.BaseFormatter;
import com.pbad.asset.decorator.CurrencyFormatter;
import com.pbad.asset.decorator.CurrencySymbolDecorator;
import com.pbad.asset.decorator.DecimalPlacesDecorator;
import com.pbad.asset.decorator.ThousandSeparatorDecorator;
import com.pbad.asset.domain.dto.CurrencyDefaultDTO;
import com.pbad.asset.domain.vo.CurrencyVO;
import com.pbad.asset.domain.vo.ExchangeRateVO;
import com.pbad.asset.service.CurrencyService;
import com.pbad.config.domain.po.UserConfigPO;
import com.pbad.config.mapper.UserConfigMapper;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 货币服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private static final String MODULE = "asset";
    private static final String DEFAULT_CURRENCY_KEY = "currency.default";
    private static final String DEFAULT_CURRENCY = "CNY";

    // 支持的货币列表
    private static final Map<String, CurrencyVO> CURRENCY_MAP = new HashMap<>();

    // 模拟汇率数据（实际应该调用外部API）
    private static final Map<String, Double> EXCHANGE_RATE_MAP = new HashMap<>();

    static {
        // 初始化货币列表
        CurrencyVO cny = new CurrencyVO();
        cny.setCode("CNY");
        cny.setName("人民币");
        cny.setSymbol("¥");
        CURRENCY_MAP.put("CNY", cny);

        CurrencyVO usd = new CurrencyVO();
        usd.setCode("USD");
        usd.setName("美元");
        usd.setSymbol("$");
        CURRENCY_MAP.put("USD", usd);

        CurrencyVO gbp = new CurrencyVO();
        gbp.setCode("GBP");
        gbp.setName("英镑");
        gbp.setSymbol("£");
        CURRENCY_MAP.put("GBP", gbp);

        // 初始化汇率（相对于CNY）
        EXCHANGE_RATE_MAP.put("CNY_USD", 0.14);  // 1 CNY = 0.14 USD
        EXCHANGE_RATE_MAP.put("CNY_GBP", 0.11);  // 1 CNY = 0.11 GBP
        EXCHANGE_RATE_MAP.put("USD_CNY", 7.14);  // 1 USD = 7.14 CNY
        EXCHANGE_RATE_MAP.put("USD_GBP", 0.79);  // 1 USD = 0.79 GBP
        EXCHANGE_RATE_MAP.put("GBP_CNY", 9.09);  // 1 GBP = 9.09 CNY
        EXCHANGE_RATE_MAP.put("GBP_USD", 1.27); // 1 GBP = 1.27 USD
    }

    private final UserConfigMapper userConfigMapper;

    @Override
    public List<CurrencyVO> getCurrencyList() {
        return CURRENCY_MAP.values().stream()
                .sorted((a, b) -> a.getCode().compareTo(b.getCode()))
                .collect(Collectors.toList());
    }

    /**
     * 使用装饰器模式格式化金额.
     *
     * 该方法主要用于演示装饰器模式在后端的落地实现，当前未通过接口直接暴露，
     * 便于后续在统计、报表等场景中复用。
     *
     * @param amount       金额
     * @param currencyCode 货币代码（如：CNY、USD、GBP）
     * @return 例如：¥1,234.56 / $1,234.56 / £1,234.56
     */
    public String formatAmount(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return null;
        }
        if (currencyCode == null) {
            currencyCode = DEFAULT_CURRENCY;
        }

        // 基础格式化：数字 → 字符串
        CurrencyFormatter formatter = new BaseFormatter();
        // 保留 2 位小数
        formatter = new DecimalPlacesDecorator(formatter, 2);
        // 千分位分隔
        formatter = new ThousandSeparatorDecorator(formatter);
        // 货币符号
        formatter = new CurrencySymbolDecorator(formatter, resolveSymbol(currencyCode));

        return formatter.format(amount);
    }

    /**
     * 根据货币代码解析符号.
     */
    private String resolveSymbol(String currencyCode) {
        CurrencyVO vo = CURRENCY_MAP.get(currencyCode);
        if (vo != null && vo.getSymbol() != null) {
            return vo.getSymbol();
        }
        // 兜底使用货币代码作为前缀
        return currencyCode + " ";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultCurrency(CurrencyDefaultDTO dto) {
        String currencyCode = dto.getCurrencyCode();
        
        // 验证货币代码是否存在
        if (!CURRENCY_MAP.containsKey(currencyCode)) {
            throw new BusinessException("40051", "货币代码不存在");
        }

        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询是否已存在配置
        UserConfigPO existingConfig = userConfigMapper.selectByUserAndKey(userId, MODULE, DEFAULT_CURRENCY_KEY);
        
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        if (existingConfig != null) {
            // 更新配置
            existingConfig.setConfigValue(currencyCode);
            existingConfig.setUpdatedAt(nowStr);
            existingConfig.setUpdatedBy(userId);
            userConfigMapper.updateConfig(existingConfig);
        } else {
            // 创建新配置
            UserConfigPO config = new UserConfigPO();
            config.setId(String.valueOf(System.currentTimeMillis()));
            config.setUserId(userId);
            config.setModule(MODULE);
            config.setConfigKey(DEFAULT_CURRENCY_KEY);
            config.setConfigValue(currencyCode);
            config.setDescription("默认货币设置");
            config.setCreatedAt(nowStr);
            config.setUpdatedAt(nowStr);
            config.setUpdatedBy(userId);
            userConfigMapper.batchInsert(Arrays.asList(config));
        }

        log.info("用户 {} 设置默认货币为 {}", userId, currencyCode);
    }

    @Override
    public String getDefaultCurrency() {
        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            return DEFAULT_CURRENCY;
        }

        UserConfigPO config = userConfigMapper.selectByUserAndKey(userId, MODULE, DEFAULT_CURRENCY_KEY);
        if (config != null && config.getConfigValue() != null) {
            return config.getConfigValue();
        }

        return DEFAULT_CURRENCY;
    }

    @Override
    public ExchangeRateVO getExchangeRate(String from, String to) {
        // 验证货币代码
        if (!CURRENCY_MAP.containsKey(from)) {
            throw new BusinessException("40051", "源货币代码不存在");
        }
        if (!CURRENCY_MAP.containsKey(to)) {
            throw new BusinessException("40051", "目标货币代码不存在");
        }

        // 相同货币，汇率为1
        if (from.equals(to)) {
            ExchangeRateVO vo = new ExchangeRateVO();
            vo.setFrom(from);
            vo.setTo(to);
            vo.setRate(1.0);
            vo.setUpdateTime(LocalDateTime.now());
            return vo;
        }

        // 获取汇率
        String rateKey = from + "_" + to;
        Double rate = EXCHANGE_RATE_MAP.get(rateKey);
        
        if (rate == null) {
            // 如果没有直接汇率，尝试通过CNY中转计算
            if (from.equals("CNY")) {
                Double toCnyRate = EXCHANGE_RATE_MAP.get(to + "_CNY");
                if (toCnyRate != null) {
                    rate = 1.0 / toCnyRate;
                }
            } else if (to.equals("CNY")) {
                rate = EXCHANGE_RATE_MAP.get(from + "_CNY");
            } else {
                // 通过CNY中转
                Double fromToCny = EXCHANGE_RATE_MAP.get(from + "_CNY");
                Double cnyToTo = EXCHANGE_RATE_MAP.get("CNY_" + to);
                if (fromToCny != null && cnyToTo != null) {
                    rate = cnyToTo / fromToCny;
                }
            }
        }

        if (rate == null) {
            throw new BusinessException("无法获取汇率信息");
        }

        ExchangeRateVO vo = new ExchangeRateVO();
        vo.setFrom(from);
        vo.setTo(to);
        vo.setRate(rate);
        vo.setUpdateTime(LocalDateTime.now());
        
        return vo;
    }
}

