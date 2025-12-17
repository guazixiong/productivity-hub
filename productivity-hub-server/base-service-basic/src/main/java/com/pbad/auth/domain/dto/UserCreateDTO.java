package com.pbad.auth.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建用户请求 DTO.
 */
@Data
public class UserCreateDTO {

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 初始密码（可选，默认使用系统默认密码）
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色列表
     */
    private List<String> roles;
}

