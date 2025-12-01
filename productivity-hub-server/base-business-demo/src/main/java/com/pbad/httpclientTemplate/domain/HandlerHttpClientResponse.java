package com.pbad.httpclientTemplate.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * http接口响应类.
 *
 * @author: pangdi
 * @date: 2023/12/28 14:16
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerHttpClientResponse {

    /**
     * 返回码。
     */
    @JsonProperty("code")
    private int code;

    /**
     * 返回消息。
     */
    @JsonProperty("msg")
    private String msg;

    /**
     * 远程调用返回值
     */
    @JsonProperty("远程调用返回值")
    private Object result;

    /**
     * 解密后的响应数据
     */
    @JsonProperty("解密后的响应数据")
    private Object decryptedResponseData;

    /**
     * 请求成功.
     *
     * @param result                远程调用返回值
     * @param decryptedResponseData 解密后的响应数据
     * @return 银盛http接口响应
     */
    public static HandlerHttpClientResponse ok(Object result, Object decryptedResponseData) {
        HandlerHttpClientResponse handlerHttpClientResponse = new HandlerHttpClientResponse();
        handlerHttpClientResponse.setCode(200);
        handlerHttpClientResponse.setMsg("请求成功");
        handlerHttpClientResponse.setResult(result);
        handlerHttpClientResponse.setDecryptedResponseData(decryptedResponseData);
        return handlerHttpClientResponse;
    }

    /**
     * 请求失败.
     *
     * @param result 错误信息
     * @return 银盛http接口响应
     */
    public static HandlerHttpClientResponse error(String result) {
        HandlerHttpClientResponse handlerHttpClientResponse = new HandlerHttpClientResponse();
        handlerHttpClientResponse.setCode(500);
        handlerHttpClientResponse.setMsg("请求失败");
        handlerHttpClientResponse.setResult(result);
        return handlerHttpClientResponse;
    }
}
