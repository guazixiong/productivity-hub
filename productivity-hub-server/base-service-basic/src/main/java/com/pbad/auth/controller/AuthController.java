package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.LoginDTO;
import com.pbad.auth.domain.vo.LoginResponseVO;
import com.pbad.auth.domain.vo.ResetPasswordResponseVO;
import com.pbad.auth.service.AuthService;
import common.core.domain.ApiResponse;
import common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
    public ApiResponse<ResetPasswordResponseVO> resetPassword(HttpServletRequest request) {
        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);

        if (token == null || !JwtUtil.validateToken(token)) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        // 从 Token 中获取用户ID
        String userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.unauthorized("Token 无效");
        }

        ResetPasswordResponseVO response = authService.resetPassword(userId);
        return ApiResponse.ok(response.getMessage(), response);
    }
}

