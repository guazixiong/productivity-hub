package com.pbad.tools.domain.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 链动小铺商品查询参数.
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LdxpGoodsQueryDTO {

    /**
     * 授权 token，留空则使用默认值.
     */
    private String token;

    /**
     * 搜索关键词.
     */
    private String keywords;

    /**
     * 分类 ID.
     */
    private Integer categoryId;

    /**
     * 商品类型.
     */
    private String goodsType;

    /**
     * 页码（从 1 开始）.
     */
    private Integer current;

    /**
     * 每页数量.
     */
    private Integer pageSize;
}

