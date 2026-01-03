package com.pbad.tools.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Cursor 邮箱自助小店商品信息.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursorCommodityVO {

    private Integer id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private Integer orderSold;

    private Integer stockState;

    private String stockStateText;
}

