package com.pbad.acl.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * ACL菜单创建DTO.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclMenuCreateDTO {
    /**
     * 幂等键（前端生成的UUID，用于防止重复提交）
     */
    @NotBlank(message = "幂等键不能为空")
    private String idempotentKey;

    /**
     * 父菜单ID（可选，NULL表示根菜单）
     */
    private Long parentId;

    /**
     * 菜单名称（1-50字符）
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 1, max = 50, message = "菜单名称长度必须在1-50个字符之间")
    private String name;

    /**
     * 路由路径（可选，1-200字符）
     */
    @Size(max = 200, message = "路由路径长度不能超过200个字符")
    private String path;

    /**
     * 组件路径（可选，1-200字符）
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    /**
     * 图标（可选，1-100字符）
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    /**
     * 菜单类型：DIR-目录，MENU-菜单
     */
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /**
     * 是否可见：0-隐藏，1-显示
     */
    @NotNull(message = "是否可见不能为空")
    private Integer visible;

    /**
     * 排序号（>=0）
     */
    @NotNull(message = "排序号不能为空")
    private Integer orderNum;
}

