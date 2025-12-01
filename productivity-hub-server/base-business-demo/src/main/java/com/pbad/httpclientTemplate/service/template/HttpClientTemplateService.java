package com.pbad.httpclientTemplate.service.template;

import com.pbad.httpclientTemplate.domain.HandlerHttpClientParam;

/**
 * http请求模板接口
 *
 * @author: pangdi
 * @date: 2023/12/27 16:00
 * @version: 1.0
 */
public interface HttpClientTemplateService {

    /**
     * post请求模板方法.
     *
     * @param handlerHttpClientParam 处理httpclient请求类
     * @return 返回结果
     */
    Object postTemplate(HandlerHttpClientParam handlerHttpClientParam);

    /**
     * get请求模板方法.
     *
     * @param handlerHttpClientParam 处理httpclient请求类
     * @return 返回结果
     */
    Object getTemplate(HandlerHttpClientParam handlerHttpClientParam);
}
