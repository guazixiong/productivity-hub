package com.pbad.basic.config.controller;

import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.vo.ConfigItemVO;
import com.pbad.config.service.ConfigService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        String userId = currentUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        List<ConfigItemVO> configList = configService.getConfigList(userId);
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
    public ApiResponse<ConfigItemVO> updateConfig(@RequestBody ConfigUpdateDTO updateDTO) {
        String userId = currentUserId();
        String username = currentUsername();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        ConfigItemVO configItem = configService.updateConfig(updateDTO, userId, username);
        return ApiResponse.ok("保存成功", configItem);
    }

    /**
     * 创建或更新配置项（通过 module 和 key）
     *
     * @param createOrUpdateDTO 创建或更新请求
     * @param request          HTTP 请求
     * @return 创建或更新后的配置项
     */
    @PostMapping("/create-or-update")
    public ApiResponse<ConfigItemVO> createOrUpdateConfig(@RequestBody ConfigCreateOrUpdateDTO createOrUpdateDTO) {
        String userId = currentUserId();
        String username = currentUsername();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        ConfigItemVO configItem = configService.createOrUpdateConfig(createOrUpdateDTO, userId, username);
        return ApiResponse.ok("保存成功", configItem);
    }

    private String currentUserId() {
        return RequestUserContext.getUserId();
    }

    private String currentUsername() {
        String username = RequestUserContext.getUsername();
        return username != null ? username : "系统";
    }
}

