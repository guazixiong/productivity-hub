package com.pbad.auth.mapper;

import com.pbad.auth.domain.po.UserPO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserPO selectByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserPO selectById(@Param("id") String id);

    /**
     * 查询全部用户
     */
    List<UserPO> selectAll();

    /**
     * 更新用户密码
     *
     * @param id       用户ID
     * @param password 新密码
     * @return 更新行数
     */
    int updatePassword(@Param("id") String id, @Param("password") String password);

    /**
     * 更新用户个人信息
     *
     * @param user 用户实体
     * @return 更新行数
     */
    int updateProfile(UserPO user);

    /**
     * 新增用户
     *
     * @param user 用户实体
     * @return 插入行数
     */
    int insertUser(UserPO user);

    /**
     * 根据用户ID删除用户
     *
     * @param id 用户ID
     * @return 删除行数
     */
    int deleteById(@Param("id") String id);
}

