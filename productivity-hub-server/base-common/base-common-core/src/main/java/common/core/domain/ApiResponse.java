package common.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * API 统一响应格式（适配需求文档格式）.
 * 格式：{code, message, data}
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 资源不存在
     */
    public static final int NOT_FOUND = 404;

    /**
     * 服务器内部错误
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     * 响应码：200=成功, 非200=失败
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> ok() {
        return restResult(null, SUCCESS, "OK");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return restResult(data, SUCCESS, "OK");
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> ok(String message) {
        return restResult(null, SUCCESS, message);
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return restResult(data, SUCCESS, message);
    }

    /**
     * 失败响应（默认消息）
     */
    public static <T> ApiResponse<T> fail() {
        return restResult(null, INTERNAL_ERROR, "操作失败");
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> ApiResponse<T> fail(String message) {
        return restResult(null, INTERNAL_ERROR, message);
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return restResult(null, code, message);
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return restResult(null, UNAUTHORIZED, message);
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return restResult(null, NOT_FOUND, message);
    }

    /**
     * 构建响应结果
     */
    private static <T> ApiResponse<T> restResult(T data, int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    /**
     * 判断是否成功
     */
    public static <T> Boolean isSuccess(ApiResponse<T> response) {
        return response != null && SUCCESS == response.getCode();
    }

    /**
     * 判断是否失败
     */
    public static <T> Boolean isError(ApiResponse<T> response) {
        return !isSuccess(response);
    }
}

