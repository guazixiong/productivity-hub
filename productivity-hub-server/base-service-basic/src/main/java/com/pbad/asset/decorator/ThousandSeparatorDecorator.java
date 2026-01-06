package com.pbad.asset.decorator;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 千分位分隔符装饰器.
 *
 * 为整数部分添加千分位分隔符，保留原有小数部分。
 */
public class ThousandSeparatorDecorator extends CurrencyFormatterDecorator {

    private final DecimalFormat decimalFormat;

    public ThousandSeparatorDecorator(CurrencyFormatter formatter) {
        super(formatter);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator(',');
        this.decimalFormat = new DecimalFormat("#,###", symbols);
    }

    @Override
    public String format(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        String formatted = super.format(amount);
        if (formatted == null) {
            return null;
        }

        // 整数部分千分位，小数部分保持不变
        BigDecimal integerPart = amount.setScale(0, BigDecimal.ROUND_DOWN);
        String integerFormatted = decimalFormat.format(integerPart);

        int dotIndex = formatted.indexOf('.');
        if (dotIndex >= 0) {
            String decimalPart = formatted.substring(dotIndex);
            return integerFormatted + decimalPart;
        }

        return integerFormatted;
    }
}


