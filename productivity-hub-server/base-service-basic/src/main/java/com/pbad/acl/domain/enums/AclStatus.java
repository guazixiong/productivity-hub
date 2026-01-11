package com.pbad.acl.domain.enums;

/**
 * ACL状态枚举.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public enum AclStatus {
    /**
     * 启用
     */
    ENABLED("ENABLED", "启用"),

    /**
     * 禁用
     */
    DISABLED("DISABLED", "禁用");

    private final String code;
    private final String description;

    AclStatus(String code, String description) {
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
    public static AclStatus fromCode(String code) {
        for (AclStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ACL status code: " + code);
    }
}

