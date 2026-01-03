package com.pbad.asset.decorator;

import java.math.BigDecimal;

/**
 * 基础格式化器（只负责将数字转为字符串）.
 */
public class BaseFormatter implements CurrencyFormatter {

    @Override
    public String format(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.toString();
    }
}


