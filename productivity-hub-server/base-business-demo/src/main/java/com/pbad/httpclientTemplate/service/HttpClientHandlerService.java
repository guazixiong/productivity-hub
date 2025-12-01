package com.pbad.httpclientTemplate.service;

import java.util.HashMap;
import java.util.Map;

/**
 * http请求调用接口
 *
 * @author: pangdi
 * @date: 2023/12/27 17:44
 * @version: 1.0
 */
public interface HttpClientHandlerService {

    /**
     * 校验参数
     *
     * @param object 请求参数
     */
    void validParameter(Object object);

    /**
     * 构建http请求url
     *
     * @param object 请求参数
     * @return url
     */
    String buildHttpUrl(Object object);

    /**
     * 构建http请求头
     *
     * @param object 请求参数
     * @return http请求头
     */
    default Map<String, String> buildHttpHeaders(Object object) {
        return new HashMap<>();
    }

    /**
     * 构建http请求内容
     *
     * @param object 请求参数
     * @return http请求内容
     */
    default Object buildHttpContent(Object object) {
        return null;
    }

    /**
     * 校验返回结果
     *
     * @param result 返回结果
     */
    default void validResponse(String result) {
    }

    /**
     * 构建返回结果
     *
     * @param result 返回结果
     * @return 返回结果
     */
    Object buildResponse(Object result);
}
