package com.pbad.generator.enums;

/**
 * 状态枚举类.
 *
 * @author: pangdi
 * @date: 2023/9/8 11:38
 * @version: 1.0
 */
public enum StatusEnum {

    /**
     * 正常
     */
    NORMAL("0","正常"),
    /**
     * 停用
     */
    STOP("1","停用"),
    ;

    /**
     * 编码
     */
    private final String code;

    /**
     * 信息
     */
    private final String message;

    StatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
