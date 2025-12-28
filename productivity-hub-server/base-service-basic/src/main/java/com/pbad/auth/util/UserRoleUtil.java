package com.pbad.auth.util;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户角色工具类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Component
@RequiredArgsConstructor
public class UserRoleUtil {

    private final UserMapper userMapper;

    private static final String ADMIN_ROLE = "admin";

    /**
     * 判断用户是否是管理员.
     *
     * @param userId 用户ID
     * @return true 如果是管理员，false 否则
     */
    public boolean isAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 检查用户名是否为 admin（忽略大小写）
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return true;
        }
        // 检查角色列表中是否包含 admin
        String roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        try {
            List<String> roleList = JSON.parseArray(roles, String.class);
            return roleList.stream().anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role));
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 判断用户是否是超级管理员（用户名必须是 admin）.
     *
     * @param userId 用户ID
     * @return true 如果是超级管理员，false 否则
     */
    public boolean isSuperAdmin(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // 只检查用户名是否为 admin（忽略大小写）
        return "admin".equalsIgnoreCase(user.getUsername());
    }
}

