package com.pbad.acl.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ACL菜单树VO（组合模式）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclMenuTreeVO {
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

    /**
     * 子菜单列表（组合模式）
     */
    private List<AclMenuTreeVO> children;

    /**
     * 添加子菜单
     *
     * @param child 子菜单
     */
    public void addChild(AclMenuTreeVO child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /**
     * 是否有子菜单
     *
     * @return true表示有子菜单
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}

