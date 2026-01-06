package com.pbad.tools.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 链动小铺商品信息.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LdxpGoodsVO {

    private String link;

    private String goodsType;

    private String goodsKey;

    private String name;

    private BigDecimal price;

    private BigDecimal marketPrice;

    private String description;

    private Long createTime;

    private String image;

    private Integer couponStatus;

    private LdxpGoodsCategoryVO category;

    private LdxpGoodsUserVO user;

    private Object discount;

    private Object multipleoffers;

    private Object fullgift;

    private LdxpGoodsExtendVO extend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class LdxpGoodsCategoryVO {
        private Integer id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class LdxpGoodsUserVO {
        private String link;
        private String nickname;
        private String avatar;
        private String token;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class LdxpGoodsExtendVO {
        private Integer stockCount;
        private Integer showStockType;
        private Integer sendOrder;
        private Integer limitCount;
    }
}

