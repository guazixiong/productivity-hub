package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.vo.ManagedUserVO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserManagementService;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclUserRoleMapper;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 系统用户管理（仅管理员）.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final UserMapper userMapper;
    private final AclUserRoleMapper aclUserRoleMapper;
    private final AclRoleMapper aclRoleMapper;

    @GetMapping
    public ApiResponse<List<ManagedUserVO>> listUsers() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅拥有管理员角色的用户可以查看用户列表");
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
            return ApiResponse.fail(403, "仅拥有管理员角色的用户可以创建用户");
        }
        ManagedUserVO user = userManagementService.createUser(dto, userId);
        return ApiResponse.ok("创建成功", user);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅拥有管理员角色的用户可以删除用户");
        }
        if (userId.equals(id)) {
            return ApiResponse.fail(400, "不能删除当前登录用户");
        }
        userManagementService.deleteUser(id, userId);
        return ApiResponse.ok("删除成功");
    }

    /**
     * 检查用户是否拥有管理员角色.
     * 优先检查ACL系统中的管理员角色，如果没有则检查传统的roles字段.
     *
     * @param userId 用户ID
     * @return true 如果用户拥有管理员角色，false 否则
     */
    private boolean isAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.debug("isAdmin: userId为空");
            return false;
        }

        // 首先检查ACL系统中是否拥有管理员角色
        try {
            List<Long> roleIds = aclUserRoleMapper.selectRoleIdsByUserId(userId);
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    AclRolePO role = aclRoleMapper.selectById(roleId);
                    if (role != null && RoleType.ADMIN.getCode().equals(role.getType())) {
                        log.debug("isAdmin: 用户 {} 通过ACL系统拥有管理员角色", userId);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("isAdmin: 检查用户 {} ACL角色失败: {}", userId, e.getMessage());
        }

        // 如果没有ACL角色，检查传统的roles字段
        UserPO current = userMapper.selectById(userId);
        if (current == null) {
            log.debug("isAdmin: 用户不存在, userId={}", userId);
            return false;
        }

        // 检查用户名是否为 admin（忽略大小写）
        if ("admin".equalsIgnoreCase(current.getUsername())) {
            log.debug("isAdmin: 用户名为admin, userId={}, username={}", userId, current.getUsername());
            return true;
        }

        // 检查角色列表中是否包含 admin
        String roles = current.getRoles();
        if (roles == null || roles.isEmpty()) {
            log.debug("isAdmin: 用户角色为空, userId={}, username={}", userId, current.getUsername());
            return false;
        }

        try {
            // 使用JSON解析检查角色列表
            boolean isAdmin = com.alibaba.fastjson.JSON.parseArray(roles, String.class)
                    .stream()
                    .anyMatch(role -> "admin".equalsIgnoreCase(role));
            log.debug("isAdmin: 解析角色列表, userId={}, username={}, roles={}, isAdmin={}",
                    userId, current.getUsername(), roles, isAdmin);
            return isAdmin;
        } catch (Exception ex) {
            log.warn("isAdmin: 解析角色列表失败, userId={}, username={}, roles={}, error={}",
                    userId, current.getUsername(), roles, ex.getMessage());
            return false;
        }
    }
}

