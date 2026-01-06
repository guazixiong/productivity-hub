package com.pbad.asset.decorator;

import java.math.BigDecimal;

/**
 * 货币格式化装饰器基类.
 *
 * 通过组合方式在基础格式化器外层叠加功能。
 */
public abstract class CurrencyFormatterDecorator implements CurrencyFormatter {

    protected final CurrencyFormatter formatter;

    protected CurrencyFormatterDecorator(CurrencyFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String format(BigDecimal amount) {
        return formatter.format(amount);
    }
}


