package com.pbad.asset.decorator;

import java.math.BigDecimal;

/**
 * 货币格式化接口.
 *
 * 对金额进行格式化，返回人类可读的字符串表示。
 */
public interface CurrencyFormatter {

    /**
     * 格式化金额
     *
     * @param amount 金额（不可为 null）
     * @return 格式化后的字符串
     */
    String format(BigDecimal amount);
}


