package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.LoginDTO;
import com.pbad.auth.domain.vo.CaptchaResponseVO;
import com.pbad.auth.domain.vo.LoginResponseVO;
import com.pbad.auth.domain.vo.ResetPasswordResponseVO;
import com.pbad.auth.service.AuthService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 获取验证码
     *
     * @return 验证码响应
     */
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponseVO> getCaptcha() {
        CaptchaResponseVO response = authService.generateCaptcha();
        return ApiResponse.ok(response);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(@RequestBody LoginDTO loginDTO) {
        LoginResponseVO response = authService.login(loginDTO);
        return ApiResponse.ok(response);
    }

    /**
     * 重置密码
     *
     * @param request HTTP 请求
     * @return 重置密码响应
     */
    @PostMapping("/reset-password")
    public ApiResponse<ResetPasswordResponseVO> resetPassword() {
        String userId = RequestUserContext.getUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        ResetPasswordResponseVO response = authService.resetPassword(userId);
        return ApiResponse.ok(response.getMessage(), response);
    }
}

