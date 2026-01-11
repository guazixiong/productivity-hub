package com.pbad.acl.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ACL角色-菜单关联 Mapper 接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclRoleMenuMapper {

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 插入角色-菜单关联
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @return 插入行数
     */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 删除角色下的所有菜单关联
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    int deleteRoleMenuByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色-菜单关联
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 插入行数
     */
    int batchInsertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}

