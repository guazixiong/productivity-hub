package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.vo.ManagedUserVO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserManagementService;
import common.core.domain.ApiResponse;
import common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public ApiResponse<List<ManagedUserVO>> listUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ApiResponse.fail(403, "仅管理员可以查看用户列表");
        }
        return ApiResponse.ok(userManagementService.listUsers());
    }

    @PostMapping
    public ApiResponse<ManagedUserVO> createUser(@RequestBody UserCreateDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ApiResponse.fail(403, "仅管理员可以创建用户");
        }
        String token = JwtUtil.extractTokenFromHeader(request.getHeader("Authorization"));
        String operatorId = JwtUtil.getUserIdFromToken(token);
        ManagedUserVO user = userManagementService.createUser(dto, operatorId);
        return ApiResponse.ok("创建成功", user);
    }

    private boolean isAdmin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);
        if (token == null || !JwtUtil.validateToken(token)) {
            return false;
        }
        String userId = JwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }
        UserPO current = userMapper.selectById(userId);
        if (current == null || current.getRoles() == null) {
            return false;
        }
        return current.getRoles().contains("\"admin\"");
    }
}

