package com.pbad.acl.controller;

import com.pbad.acl.domain.dto.AclUserRoleBindDTO;
import com.pbad.acl.service.AclUserRoleService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * ACL用户-角色控制器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/acl/users")
@RequiredArgsConstructor
@Validated
public class AclUserRoleController {

    private final AclUserRoleService userRoleService;

    /**
     * 绑定用户-角色关系（覆盖式绑定，多角色并集）
     * API-REQ-003-01: POST /acl/users/bind-roles
     *
     * @param bindDTO 绑定DTO
     * @return 绑定结果
     */
    @PostMapping("/bind-roles")
    public ApiResponse<Void> bindUserRoles(@Valid @RequestBody AclUserRoleBindDTO bindDTO) {
        userRoleService.bindUserRoles(bindDTO);
        return ApiResponse.ok(null);
    }

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @GetMapping("/roles")
    public ApiResponse<List<Long>> getUserRoles(@RequestParam String userId) {
        List<Long> roleIds = userRoleService.getRoleIdsByUserId(userId);
        return ApiResponse.ok(roleIds);
    }

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    @GetMapping("/by-role")
    public ApiResponse<List<String>> getUsersByRole(@RequestParam Long roleId) {
        List<String> userIds = userRoleService.getUserIdsByRoleId(roleId);
        return ApiResponse.ok(userIds);
    }
}

