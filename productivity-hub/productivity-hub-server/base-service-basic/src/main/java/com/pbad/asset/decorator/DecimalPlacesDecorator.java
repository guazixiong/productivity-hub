package com.pbad.asset.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 小数位数装饰器.
 *
 * 负责对金额进行四舍五入并保留固定小数位数。
 */
public class DecimalPlacesDecorator extends CurrencyFormatterDecorator {

    private final int decimalPlaces;

    public DecimalPlacesDecorator(CurrencyFormatter formatter, int decimalPlaces) {
        super(formatter);
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public String format(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        BigDecimal rounded = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return super.format(rounded);
    }
}


