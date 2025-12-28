package common.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Http请求响应类
 *
 * @author: pbad
 * @date: 2024年06月05日 15:36
 */
@Data
public class HttpClientResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    /**
     * 请求参数
     */
    private String params;

    /**
     * 响应编码
     */
    private int code;

    /**
     * 请求结果
     */
    private String result;

    /**
     * 请求结果对象
     */
    private Object data;

    /**
     * 请求结果解密值
     */
    private String decryptResult;

    /**
     * 构造.
     *
     * @param params        请求参数
     * @param params        响应编码
     * @param params        请求结果
     * @param decryptResult 解密结果
     * @return 响应结果
     */
    private static HttpClientResponse restResult(String params, int code, String result, String decryptResult) {
        HttpClientResponse httpClientResponse = new HttpClientResponse();
        httpClientResponse.setParams(params);
        httpClientResponse.setCode(code);
        httpClientResponse.setResult(result);
        httpClientResponse.setDecryptResult(decryptResult);
        return httpClientResponse;
    }

    /**
     * 成功.
     *
     * @param params 请求参数
     * @param result 请求结果
     * @return 响应结果
     */
    public static HttpClientResponse ok(String params, String result) {
        return restResult(params, SUCCESS, result, null);
    }

    /**
     * 成功.
     *
     * @param params 请求参数
     * @param result 请求结果
     * @return 响应结果
     */
    public static HttpClientResponse ok(String params, String result, String decryptResult) {
        return restResult(params, SUCCESS, result, decryptResult);
    }

    /**
     * 失败.
     *
     * @param params 请求参数
     * @param result 请求结果
     * @return 响应结果
     */
    public static HttpClientResponse fail(String params, String result) {
        return restResult(params, FAIL, result, null);
    }

    /**
     * 失败.
     *
     * @param params 请求参数
     * @param code   响应编码
     * @param result 请求结果
     * @return 响应结果
     */
    public static HttpClientResponse fail(String params, int code, String result) {
        return restResult(params, code, result, null);
    }

}
