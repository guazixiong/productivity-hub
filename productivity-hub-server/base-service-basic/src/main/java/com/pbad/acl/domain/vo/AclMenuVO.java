package com.pbad.acl.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * ACL菜单VO.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclMenuVO {
    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 父菜单ID（NULL表示根菜单）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 菜单类型：DIR-目录，MENU-菜单
     */
    private String type;

    /**
     * 是否可见：0-隐藏，1-显示
     */
    private Integer visible;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 状态：ENABLED-启用，DISABLED-禁用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

