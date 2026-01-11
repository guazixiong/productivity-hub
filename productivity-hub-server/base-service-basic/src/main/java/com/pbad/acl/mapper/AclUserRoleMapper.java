package com.pbad.acl.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ACL用户-角色关联 Mapper 接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclUserRoleMapper {

    /**
     * 根据用户ID查询角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") String userId);

    /**
     * 插入用户-角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 插入行数
     */
    int insertUserRole(@Param("userId") String userId, @Param("roleId") Long roleId);

    /**
     * 删除用户下的所有角色关联
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteUserRoleByUserId(@Param("userId") String userId);

    /**
     * 批量插入用户-角色关联
     *
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 插入行数
     */
    int batchInsertUserRole(@Param("userId") String userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<String> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}

