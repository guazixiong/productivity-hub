package com.pbad.httpclientTemplate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 处理httpclient请求类
 *
 * @author: pangdi
 * @date: 2023/12/28 09:19
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HandlerHttpClientParam {

    /**
     * 请求方法
     */
    private String method;

    /**
     * 入参
     */
    private Object param;

    /**
     * 业务id(唯一性id)
     * 可用作接口幂等性,日志存储
     */
    private String businessId;

    /**
     * 自定义传参,可用于自定义模板
     */
    private Map<String, Object> customerMap;

    @Override
    public String toString() {
        return "HandlerHttpClientParam{" +
                "method='" + method + '\'' +
                ", param=" + param +
                ", businessId='" + businessId + '\'' +
                ", customerMap=" + customerMap +
                '}';
    }
}
