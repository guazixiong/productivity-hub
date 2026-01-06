package com.pbad.asset.domain.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 心愿单实体类（PO）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class WishlistPO {
    /**
     * 心愿单ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 心愿名称
     */
    private String name;

    /**
     * 心愿价格
     */
    private BigDecimal price;

    /**
     * 心愿链接
     */
    private String link;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否已实现
     */
    private Boolean achieved;

    /**
     * 实现时间
     */
    private Date achievedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

