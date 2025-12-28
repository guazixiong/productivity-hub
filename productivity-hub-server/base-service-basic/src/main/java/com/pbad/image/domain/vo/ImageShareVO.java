package com.pbad.image.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 图片分享视图对象.
 * 关联需求：REQ-IMG-006
 * 关联接口：API-REQ-IMG-006-01
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class ImageShareVO {

    /**
     * 图片ID
     */
    private String id;

    /**
     * 分享令牌
     */
    private String shareToken;

    /**
     * 分享链接
     */
    private String shareUrl;

    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}

