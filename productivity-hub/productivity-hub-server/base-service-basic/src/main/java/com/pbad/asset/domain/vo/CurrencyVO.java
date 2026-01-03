package com.pbad.asset.domain.vo;

import lombok.Data;

/**
 * 货币视图对象（VO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CurrencyVO {
    /**
     * 货币代码
     */
    private String code;

    /**
     * 货币名称
     */
    private String name;

    /**
     * 货币符号
     */
    private String symbol;
}

