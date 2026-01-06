package com.pbad.generator.exception;

/**
 * 异常枚举类.
 *
 * @author: pbad
 * @date: 2023/8/30 15:53
 * @version: 1.0
 */
public enum IdExceptionMsgEnum {

    /**
     * 模块唯一标识为空
     */
    MODULE_KEY_IS_NULL("11001","模块唯一标识为空"),
    /**
     * 获取模块生成唯一标识失败
     */
    GET_ID_GENERATOR_DEFEAT("11002","获取模块生成唯一标识失败"),
    ;

    /**
     * 错误编码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    IdExceptionMsgEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
