package com.pbad.httpclientTemplate.domain.factory;

import com.pbad.httpclientTemplate.service.HttpClientHandlerService;
import common.util.judge.JudgeParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * http请求调用接口工厂类.
 *
 * @author: pangdi
 * @date: 2023/12/27 17:07
 * @version: 1.0
 */
@Component
public class HttpClientTemplateFactory {

    /**
     * http请求调用接口实现类
     * map<beanName,serviceImpl>
     */
    @Autowired
    private Map<String, HttpClientHandlerService> httpClientHandlerServiceMap;

    /**
     * 获取service服务.
     *
     * @param handlerBean 请求方法
     * @return service接口
     */
    private HttpClientHandlerService getService(String handlerBean) {
        JudgeParameterUtil.checkNotNullOrEmpty(handlerBean, "506", "beanName为空,请联系管理员进行处理");
        return httpClientHandlerServiceMap.getOrDefault(handlerBean, null);
    }

    /**
     * 参数校验方法.
     *
     * @param handlerBean 处理bean
     * @param object      请求参数
     */
    public void validParameter(String handlerBean, Object object) {
        getService(handlerBean).validParameter(object);
    }

    /**
     * 构造请求header.
     *
     * @param handlerBean 处理bean
     * @param object      请求参数
     * @return 请求header
     */
    public Map<String, String> buildHttpHeaders(String handlerBean, Object object) {
        return getService(handlerBean).buildHttpHeaders(object);
    }

    /**
     * 构造请求url.
     *
     * @param handlerBean 处理bean
     * @param object      请求参数
     * @return 请求url
     */
    public String buildHttpUrl(String handlerBean, Object object) {
        return getService(handlerBean).buildHttpUrl(object);
    }

    /**
     * 构造请求body.
     *
     * @param handlerBean 处理bean
     * @param object      请求参数
     * @return 请求body
     */
    public Object buildHttpContent(String handlerBean, Object object) {
        return getService(handlerBean).buildHttpContent(object);
    }

    /**
     * 校验返回结果.
     *
     * @param handlerBean 处理bean
     * @param result      返回结果
     */
    public void validResponse(String handlerBean, String result) {
        getService(handlerBean).validResponse(result);
    }

    /**
     * 构造返回结果.
     *
     * @param method 处理bean
     * @param result 返回结果
     * @return 返回结果
     */
    public Object buildResponse(String method, Object result) {
        return getService(method).buildResponse(result);
    }

}
