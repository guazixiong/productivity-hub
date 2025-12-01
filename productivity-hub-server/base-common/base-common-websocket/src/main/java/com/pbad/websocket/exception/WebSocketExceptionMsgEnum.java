package com.pbad.websocket.exception;

/**
 * 异常枚举类.
 *
 * @author: pangdi
 * @date: 2023/8/30 15:53
 * @version: 1.0
 */
public enum WebSocketExceptionMsgEnum {

    /**
     * 推送账户不能为空
     */
    USER_ACCOUNT_IS_NULL("11001","推送账户不能为空"),
    /**
     * 推送消息不能为空
     */
    MESSAGE_IS_NULL("11002","推送消息不能为空"),
    /**
     * 推送消息客户端不能为空
     */
    CLIENT_IS_NULL("11003","推送消息客户端不能为空"),
    ;

    /**
     * 错误编码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    WebSocketExceptionMsgEnum(String errorCode, String errorMessage) {
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
