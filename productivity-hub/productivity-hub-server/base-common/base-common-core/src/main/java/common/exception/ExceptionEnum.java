package common.exception;

/**
 * 数据操作错误定义枚举类.
 *
 * @author: pbad
 * @date: 2023/9/8 14:59
 * @version: 1.0
 */
public enum ExceptionEnum implements BaseErrorInfoInterface {

    /**
     * 成功!
     */
    SUCCESS("2000", "成功!"),
    /**
     * 请求的数据格式不符!
     */
    BODY_NOT_MATCH("4000", "请求的数据格式不符!"),
    /**
     * 请求的数字签名不匹配!
     */
    SIGNATURE_NOT_MATCH("4001", "请求的数字签名不匹配!"),
    /**
     * 未找到该资源!
     */
    NOT_FOUND("4004", "未找到该资源!"),
    /**
     * 服务器内部错误!
     */
    INTERNAL_SERVER_ERROR("5000", "服务器内部错误!"),
    /**
     * 服务器正忙，请稍后再试!
     */
    SERVER_BUSY("5003", "服务器正忙，请稍后再试!"),
    /**
     * 类型转换异常!
     */
    PARAMS_NOT_CONVERT("5101", "类型转换异常!");

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误描述
     */
    private final String errorMessage;

    ExceptionEnum(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}
