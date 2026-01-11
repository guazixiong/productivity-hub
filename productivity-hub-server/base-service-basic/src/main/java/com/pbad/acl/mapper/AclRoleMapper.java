package com.pbad.acl.mapper;

import com.pbad.acl.domain.po.AclRolePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ACL角色 Mapper 接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclRoleMapper {

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<AclRolePO> selectAll();

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色
     */
    AclRolePO selectById(@Param("id") Long id);

    /**
     * 根据名称查询角色
     *
     * @param name 角色名称
     * @return 角色
     */
    AclRolePO selectByName(@Param("name") String name);

    /**
     * 插入角色
     *
     * @param role 角色
     * @return 插入行数
     */
    int insertRole(AclRolePO role);

    /**
     * 更新角色
     *
     * @param role 角色
     * @return 更新行数
     */
    int updateRole(AclRolePO role);

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 删除行数
     */
    int deleteRole(@Param("id") Long id);

    /**
     * 检查角色是否被用户占用
     *
     * @param roleId 角色ID
     * @return 占用数量
     */
    int countUserRoleByRoleId(@Param("roleId") Long roleId);
}

