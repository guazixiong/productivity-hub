package com.pbad.asset.decorator;

import java.math.BigDecimal;

/**
 * 货币符号装饰器.
 *
 * 在已格式化的字符串前添加货币符号（¥、$、£ 等）。
 */
public class CurrencySymbolDecorator extends CurrencyFormatterDecorator {

    private final String symbol;

    public CurrencySymbolDecorator(CurrencyFormatter formatter, String symbol) {
        super(formatter);
        this.symbol = symbol;
    }

    @Override
    public String format(BigDecimal amount) {
        String formatted = super.format(amount);
        if (formatted == null) {
            return null;
        }
        return symbol + formatted;
    }
}


