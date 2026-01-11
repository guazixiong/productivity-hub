package com.pbad.acl.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * ACL角色-菜单绑定DTO.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclRoleMenuBindDTO {
    /**
     * 角色ID
     */
    @NotBlank(message = "角色ID不能为空")
    private String roleId;

    /**
     * 菜单ID列表（覆盖式绑定，可以为空数组表示清空所有绑定）
     */
    @NotNull(message = "菜单ID列表不能为null")
    private List<Long> menuIds;
}

