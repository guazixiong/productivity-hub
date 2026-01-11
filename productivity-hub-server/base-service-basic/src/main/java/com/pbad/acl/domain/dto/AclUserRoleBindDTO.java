package com.pbad.acl.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * ACL用户-角色绑定DTO.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclUserRoleBindDTO {
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 角色ID列表（覆盖式绑定，多角色并集）
     */
    @NotNull(message = "角色ID列表不能为空")
    private List<Long> roleIds;
}

