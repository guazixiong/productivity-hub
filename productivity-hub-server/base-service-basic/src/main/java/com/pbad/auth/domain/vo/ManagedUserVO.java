package com.pbad.auth.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理后台用户信息 VO.
 */
@Data
public class ManagedUserVO {
    private String id;
    private String username;
    private String name;
    private String email;
    private List<String> roles;
    private String createdAt;
    private String updatedAt;
}

