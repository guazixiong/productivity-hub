package com.pbad.acl.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * ACL角色实体类（PO）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Data
public class AclRolePO {
    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称（唯一）
     */
    private String name;

    /**
     * 角色类型：ADMIN-管理员，USER-普通用户，CUSTOM-自定义
     */
    private String type;

    /**
     * 状态：ENABLED-启用，DISABLED-禁用
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}

