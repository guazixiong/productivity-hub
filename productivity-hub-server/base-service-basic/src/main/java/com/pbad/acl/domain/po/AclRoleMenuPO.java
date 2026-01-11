package com.pbad.acl.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * ACL角色-菜单关联实体类（PO）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclRoleMenuPO {
    /**
     * 关联ID
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

    /**
     * 创建时间
     */
    private Date createdAt;
}

