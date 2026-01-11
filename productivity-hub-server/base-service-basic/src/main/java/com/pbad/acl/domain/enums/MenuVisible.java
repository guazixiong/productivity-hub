package com.pbad.acl.domain.enums;

/**
 * 菜单可见性枚举.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public enum MenuVisible {
    /**
     * 隐藏
     */
    HIDDEN(0, "隐藏"),

    /**
     * 显示
     */
    VISIBLE(1, "显示");

    private final int code;
    private final String description;

    MenuVisible(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static MenuVisible fromCode(int code) {
        for (MenuVisible visible : values()) {
            if (visible.code == code) {
                return visible;
            }
        }
        throw new IllegalArgumentException("Unknown menu visible code: " + code);
    }
}

