package com.pbad.auth.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 用户个人信息更新 DTO.
 *
 * @author: pbad
 * @date: 2025-12-XX
 * @version: 1.0
 */
@Data
public class UserProfileUpdateDTO {
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
}

