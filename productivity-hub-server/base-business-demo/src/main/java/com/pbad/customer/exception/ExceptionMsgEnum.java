package com.pbad.customer.exception;

/**
 * 异常枚举类.
 *
 * @author: pangdi
 * @date: 2023/8/30 15:53
 * @version: 1.0
 */
public enum ExceptionMsgEnum {

    /**
     * 获取银行卡信息失败
     */
    NOT_FOUND_BANK_CARD("10001","获取银行卡信息失败"),
    /**
     * 银行卡已绑定
     */
    BANK_CARD_HAVE_BIND("10002","银行卡已绑定"),
    /**
     * 客商编号为空
     */
    CUSTOMER_ID_IS_NULL("10003","客商编号为空"),
    /**
     * 操作人为空
     */
    OPERATE_IS_NULL("10004","操作人为空"),
    /**
     * 获取客商信息失败
     */
    NOT_FOUND_CUSTOMER_INFO("10005","获取客商信息失败"),
    ;

    /**
     * 错误编码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    ExceptionMsgEnum(String errorCode, String errorMessage) {
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
