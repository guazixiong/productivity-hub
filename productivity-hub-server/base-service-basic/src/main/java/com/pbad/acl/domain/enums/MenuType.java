package com.pbad.acl.domain.enums;

/**
 * 菜单类型枚举.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public enum MenuType {
    /**
     * 目录
     */
    DIR("DIR", "目录"),

    /**
     * 菜单
     */
    MENU("MENU", "菜单");

    private final String code;
    private final String description;

    MenuType(String code, String description) {
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
    public static MenuType fromCode(String code) {
        for (MenuType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown menu type code: " + code);
    }
}

