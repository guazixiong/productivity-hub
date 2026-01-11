package com.pbad.acl.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * ACL角色创建DTO.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclRoleCreateDTO {
    /**
     * 幂等键（前端生成的UUID，用于防止重复提交）
     */
    @NotBlank(message = "幂等键不能为空")
    private String idempotentKey;

    /**
     * 角色名称（1-50字符）
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 50, message = "角色名称长度必须在1-50个字符之间")
    private String name;

    /**
     * 角色类型：ADMIN-管理员，USER-普通用户，CUSTOM-自定义
     */
    @NotBlank(message = "角色类型不能为空")
    private String type;

    /**
     * 备注（可选，最大200字符）
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
}

