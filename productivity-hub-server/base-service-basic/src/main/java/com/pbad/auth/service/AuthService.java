package com.pbad.auth.service;

import com.pbad.auth.domain.dto.LoginDTO;
import com.pbad.auth.domain.vo.CaptchaResponseVO;
import com.pbad.auth.domain.vo.LoginResponseVO;
import com.pbad.auth.domain.vo.ResetPasswordResponseVO;

/**
 * 认证服务接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface AuthService {

    /**
     * 生成验证码
     *
     * @return 验证码响应（包含验证码Key和图片）
     */
    CaptchaResponseVO generateCaptcha();

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 登录响应（包含 Token 和用户信息）
     */
    LoginResponseVO login(LoginDTO loginDTO);

    /**
     * 重置密码
     *
     * @param userId 用户ID
     * @return 重置密码响应
     */
    ResetPasswordResponseVO resetPassword(String userId);
}

