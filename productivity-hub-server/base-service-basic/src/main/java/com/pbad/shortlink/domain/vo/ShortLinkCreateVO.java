package com.pbad.shortlink.domain.vo;

import lombok.Data;

/**
 * 创建短链请求VO.
 *
 * @author pbad
 */
@Data
public class ShortLinkCreateVO {
    /**
     * 原始URL
     */
    private String originalUrl;
}

