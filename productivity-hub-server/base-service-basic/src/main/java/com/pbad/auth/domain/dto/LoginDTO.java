package com.pbad.auth.domain.dto;

import lombok.Data;

/**
 * 登录请求 DTO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class LoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 验证码（可选）
     */
    private String captcha;
}

