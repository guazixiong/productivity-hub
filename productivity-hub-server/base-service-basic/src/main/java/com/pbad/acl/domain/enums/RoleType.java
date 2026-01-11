package com.pbad.acl.domain.enums;

/**
 * 角色类型枚举.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public enum RoleType {
    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),

    /**
     * 普通用户
     */
    USER("USER", "普通用户"),

    /**
     * 自定义
     */
    CUSTOM("CUSTOM", "自定义");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static RoleType fromCode(String code) {
        for (RoleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role type code: " + code);
    }
}

