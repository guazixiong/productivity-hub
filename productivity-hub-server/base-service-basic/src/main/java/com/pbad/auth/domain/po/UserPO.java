package com.pbad.auth.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 用户实体类（PO）.
 *
 * @author: pbad
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
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别（男/女/其他）
     */
    private String gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 地址
     */
    private String address;

    /**
     * 公司/组织
     */
    private String company;

    /**
     * 职位
     */
    private String position;

    /**
     * 个人网站
     */
    private String website;

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

