package com.pbad.auth.service;

import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.vo.ManagedUserVO;

import java.util.List;

/**
 * 系统用户管理服务.
 */
public interface UserManagementService {

    /**
     * 查询全部用户
     */
    List<ManagedUserVO> listUsers();

    /**
     * 创建用户并初始化配置
     *
     * @param dto      创建请求
     * @param operator 当前操作人ID
     */
    ManagedUserVO createUser(UserCreateDTO dto, String operator);

    /**
     * 删除用户及其所有相关数据
     *
     * @param userId   要删除的用户ID
     * @param operator 当前操作人ID
     */
    void deleteUser(String userId, String operator);
}

