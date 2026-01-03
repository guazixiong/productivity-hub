package com.pbad.auth.domain.vo;

import lombok.Data;

/**
 * 验证码响应 VO.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Data
public class CaptchaResponseVO {
    /**
     * 验证码Key（用于验证时匹配）
     */
    private String key;

    /**
     * 验证码图片（Base64编码）
     */
    private String image;
}

