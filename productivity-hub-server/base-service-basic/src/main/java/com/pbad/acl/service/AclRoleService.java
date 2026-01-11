package com.pbad.acl.service;

import com.pbad.acl.domain.dto.AclRoleCreateDTO;
import com.pbad.acl.domain.dto.AclRoleMenuBindDTO;
import com.pbad.acl.domain.dto.AclRoleUpdateDTO;
import com.pbad.acl.domain.vo.AclRoleVO;

import java.util.List;

/**
 * ACL角色服务接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclRoleService {

    /**
     * 查询所有角色列表
     *
     * @return 角色列表
     */
    List<AclRoleVO> getAllRoles();

    /**
     * 根据ID获取角色详情（包含绑定的菜单ID列表）
     *
     * @param id 角色ID
     * @return 角色详情
     */
    AclRoleVO getRoleById(Long id);

    /**
     * 创建角色
     *
     * @param createDTO 创建DTO
     * @return 创建的角色
     */
    AclRoleVO createRole(AclRoleCreateDTO createDTO);

    /**
     * 更新角色
     *
     * @param updateDTO 更新DTO
     * @return 更新后的角色
     */
    AclRoleVO updateRole(AclRoleUpdateDTO updateDTO);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 绑定角色-菜单关系（覆盖式绑定）
     *
     * @param bindDTO 绑定DTO
     */
    void bindRoleMenus(AclRoleMenuBindDTO bindDTO);
}

