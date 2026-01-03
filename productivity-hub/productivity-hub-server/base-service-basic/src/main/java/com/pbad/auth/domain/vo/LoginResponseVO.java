package com.pbad.auth.domain.vo;

import lombok.Data;

/**
 * 登录响应 VO.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class LoginResponseVO {
    /**
     * Token
     */
    private String token;

    /**
     * 刷新 Token
     */
    private String refreshToken;

    /**
     * 用户信息
     */
    private UserVO user;
}

