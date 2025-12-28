package com.pbad.shortlink.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 短链实体类（PO）.
 *
 * @author pbad
 */
@Data
public class ShortLinkPO {
    /**
     * 短链ID
     */
    private String id;

    /**
     * 短链代码（唯一，用于系统内部路由）
     */
    private String shortCode;

    /**
     * 原始URL
     */
    private String originalUrl;

    /**
     * 第三方短链完整URL（由第三方API返回）
     */
    private String shortLinkUrl;

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

