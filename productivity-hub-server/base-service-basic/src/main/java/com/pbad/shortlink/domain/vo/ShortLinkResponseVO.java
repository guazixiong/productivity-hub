package com.pbad.shortlink.domain.vo;

import lombok.Data;

/**
 * 短链响应VO.
 *
 * @author pbad
 */
@Data
public class ShortLinkResponseVO {
    /**
     * 短链代码
     */
    private String shortCode;

    /**
     * 短链完整URL
     */
    private String shortLinkUrl;

    /**
     * 原始URL
     */
    private String originalUrl;
}

