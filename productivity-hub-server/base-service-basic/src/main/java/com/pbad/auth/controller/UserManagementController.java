package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.vo.ManagedUserVO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserManagementService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 系统用户管理（仅管理员）.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final UserMapper userMapper;

    @GetMapping
    public ApiResponse<List<ManagedUserVO>> listUsers() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以查看用户列表");
        }
        return ApiResponse.ok(userManagementService.listUsers());
    }

    @PostMapping
    public ApiResponse<ManagedUserVO> createUser(@RequestBody UserCreateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以创建用户");
        }
        ManagedUserVO user = userManagementService.createUser(dto, userId);
        return ApiResponse.ok("创建成功", user);
    }

    private boolean isAdmin(String userId) {
        UserPO current = userMapper.selectById(userId);
        if (current == null || current.getRoles() == null) {
            return false;
        }
        return current.getRoles().contains("\"admin\"");
    }
}

