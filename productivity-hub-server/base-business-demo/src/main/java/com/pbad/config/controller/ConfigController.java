package com.pbad.config.controller;

import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.vo.ConfigItemVO;
import com.pbad.config.service.ConfigService;
import common.core.domain.ApiResponse;
import common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配置控制器.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 获取配置列表
     *
     * @return 配置项列表
     */
    @GetMapping
    public ApiResponse<List<ConfigItemVO>> getConfigList() {
        List<ConfigItemVO> configList = configService.getConfigList();
        return ApiResponse.ok(configList);
    }

    /**
     * 更新配置项
     *
     * @param updateDTO 更新请求
     * @param request   HTTP 请求
     * @return 更新后的配置项
     */
    @PutMapping
    public ApiResponse<ConfigItemVO> updateConfig(@RequestBody ConfigUpdateDTO updateDTO,
                                                   HttpServletRequest request) {
        // 从请求头获取 Token
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);

        if (token == null || !JwtUtil.validateToken(token)) {
            return ApiResponse.unauthorized("Token 无效或过期");
        }

        // 从 Token 中获取用户名
        String username = JwtUtil.getUsernameFromToken(token);
        if (username == null) {
            username = "系统";
        }

        ConfigItemVO configItem = configService.updateConfig(updateDTO, username);
        return ApiResponse.ok("保存成功", configItem);
    }
}

