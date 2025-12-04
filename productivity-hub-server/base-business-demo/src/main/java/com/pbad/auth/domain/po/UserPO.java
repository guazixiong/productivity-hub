package com.pbad.auth.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类（PO）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class UserPO {
    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密）
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
     * 角色（JSON数组字符串）
     */
    private String roles;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

