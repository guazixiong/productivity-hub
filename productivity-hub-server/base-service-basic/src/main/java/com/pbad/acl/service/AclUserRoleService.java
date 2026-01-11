package com.pbad.acl.service;

import com.pbad.acl.domain.dto.AclUserRoleBindDTO;

import java.util.List;

/**
 * ACL用户-角色服务接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclUserRoleService {

    /**
     * 绑定用户-角色关系（覆盖式绑定，多角色并集）
     *
     * @param bindDTO 绑定DTO
     */
    void bindUserRoles(AclUserRoleBindDTO bindDTO);

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getRoleIdsByUserId(String userId);

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<String> getUserIdsByRoleId(Long roleId);
}

