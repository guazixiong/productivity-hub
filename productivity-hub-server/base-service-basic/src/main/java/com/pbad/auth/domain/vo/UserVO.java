package com.pbad.auth.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 邮箱
     */
    private String email;
}

