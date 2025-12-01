package com.pbad.httpclientTemplate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * http请求代理类.
 *
 * @author: pangdi
 * @date: 2023/12/28 15:22
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HandlerHttpClientProxyParam {


    /**
     * http远程调用请求模板bean名称
     */
    private String proxyTemplateBeanName;

    /**
     * 代理方法
     */
    private String method;

    /**
     * 代理入参
     */
    private Object param;

    /**
     * 响应值
     */
    private Object result;

    /**
     * 业务id(唯一性id)
     * 可用作接口幂等性,日志存储
     */
    private String businessId;

    /**
     * 自定义传参
     * map<String,value>
     */
    private Map<String, Object> customerMap;
}
