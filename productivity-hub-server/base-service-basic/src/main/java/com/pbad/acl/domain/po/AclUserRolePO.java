package com.pbad.acl.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * ACL用户-角色关联实体类（PO）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclUserRolePO {
    /**
     * 关联ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private Date createdAt;
}

