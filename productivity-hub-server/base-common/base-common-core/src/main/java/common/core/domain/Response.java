package common.core.domain;

import common.constant.HttpStatus;
import common.exception.BaseErrorInfoInterface;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应信息主体.
 *
 * @author: pangdi
 * @date: 2023/8/30 17:15
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class Response<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    private int code;

    private String msg;

    private T data;

    public static <T> Response<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> Response<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> Response<T> ok(String msg) {
        return restResult(null, SUCCESS, msg);
    }

    public static <T> Response<T> ok(String msg, T data) {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> Response<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> Response<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> Response<T> fail(T data) {
        return restResult(data, FAIL, "操作失败");
    }

    public static <T> Response<T> fail(BaseErrorInfoInterface errorInfo) {
        Response<T> response = new Response<T>();
        response.setCode(Integer.parseInt(errorInfo.getErrorCode()));
        response.setMsg(errorInfo.getErrorMessage());
        return response;
    }

    public static <T> Response<T> fail(String msg, T data) {
        return restResult(data, FAIL, msg);
    }

    public static <T> Response<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> Response<T> warn(String msg) {
        return restResult(null, HttpStatus.WARN, msg);
    }

    /**
     * 返回警告消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> Response<T> warn(String msg, T data) {
        return restResult(data, HttpStatus.WARN, msg);
    }

    private static <T> Response<T> restResult(T data, int code, String msg) {
        Response<T> response = new Response<T>();
        response.setCode(code);
        response.setData(data);
        response.setMsg(msg);
        return response;
    }

    public static <T> Boolean isError(Response<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(Response<T> ret) {
        return Response.SUCCESS == ret.getCode();
    }
}
