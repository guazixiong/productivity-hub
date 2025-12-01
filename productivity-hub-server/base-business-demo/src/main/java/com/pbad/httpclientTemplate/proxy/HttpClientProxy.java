package com.pbad.httpclientTemplate.proxy;

import com.pbad.httpclientTemplate.domain.HandlerHttpClientParam;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientProxyParam;
import com.pbad.httpclientTemplate.domain.HttpClientTemplateServiceEnum;
import com.pbad.httpclientTemplate.service.template.HttpClientTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * http请求代理类
 *
 * @author: pangdi
 * @date: 2023/12/28 14:43
 * @version: 1.0
 */
@Component
public class HttpClientProxy {

    /**
     * 请求模板服务
     * map<beanName,serviceImpl>
     */
    @Autowired
    private Map<String, HttpClientTemplateService> httpClientTemplateServiceMap;

    /**
     * http请求方式
     * get: get请求
     * post: post请求
     */
    private static final String GET_HTTP_TYPE = "GET";
    private static final String POST_HTTP_TYPE = "POST";

    /**
     * 获取service服务.
     *
     * @param beanName bean名称
     * @return service接口
     */
    private HttpClientTemplateService getService(String beanName) {
        // 当beanName为空时,使用默认模板
        if (beanName == null || "".equals(beanName)) {
            beanName = HttpClientTemplateServiceEnum.COMMON_HTTP_TEMPLATE.getBeanName();
        }
        HttpClientTemplateServiceEnum httpClientTemplateServiceEnum = HttpClientTemplateServiceEnum.getEnumByBean(beanName);
        return httpClientTemplateServiceMap.getOrDefault(httpClientTemplateServiceEnum.getBeanName(), null);
    }

    /**
     * get请求模板
     *
     * @param handlerHttpClientProxyParam http请求代理类
     * @return 响应结果
     */
    public Object getHttp(HandlerHttpClientProxyParam handlerHttpClientProxyParam) {
        HttpClientTemplateService httpClientTemplateService = getService(handlerHttpClientProxyParam.getProxyTemplateBeanName());
        HandlerHttpClientParam handlerHttpClientParam = new HandlerHttpClientParam();
        handlerHttpClientParam.setMethod(handlerHttpClientProxyParam.getMethod());
        handlerHttpClientParam.setParam(handlerHttpClientProxyParam.getParam());
        handlerHttpClientParam.setBusinessId(handlerHttpClientParam.getBusinessId());
        handlerHttpClientParam.setCustomerMap(handlerHttpClientParam.getCustomerMap());
        return httpClientTemplateService.getTemplate(handlerHttpClientParam);
    }

    /**
     * post json请求模板
     *
     * @param handlerHttpClientProxyParam http请求代理类
     * @return 响应结果
     */
    public Object postJsonHttp(HandlerHttpClientProxyParam handlerHttpClientProxyParam) {
        HttpClientTemplateService httpClientTemplateService = getService(handlerHttpClientProxyParam.getProxyTemplateBeanName());
        HandlerHttpClientParam handlerHttpClientParam = new HandlerHttpClientParam();
        handlerHttpClientParam.setMethod(handlerHttpClientProxyParam.getMethod());
        handlerHttpClientParam.setParam(handlerHttpClientProxyParam.getParam());
        handlerHttpClientParam.setBusinessId(handlerHttpClientParam.getBusinessId());
        handlerHttpClientParam.setCustomerMap(handlerHttpClientParam.getCustomerMap());
        return httpClientTemplateService.postTemplate(handlerHttpClientParam);
    }

    /**
     * post 上传图片请求模板
     *
     * @param handlerHttpClientProxyParam http请求代理类
     * @return 响应结果
     */
    public Object postPicJsonHttp(HandlerHttpClientProxyParam handlerHttpClientProxyParam) {
        HttpClientTemplateService httpClientTemplateService = getService(handlerHttpClientProxyParam.getProxyTemplateBeanName());
        HandlerHttpClientParam handlerHttpClientParam = new HandlerHttpClientParam();
        handlerHttpClientParam.setMethod(handlerHttpClientProxyParam.getMethod());
        handlerHttpClientParam.setParam(handlerHttpClientProxyParam.getParam());
        handlerHttpClientParam.setBusinessId(handlerHttpClientParam.getBusinessId());
        handlerHttpClientParam.setCustomerMap(handlerHttpClientParam.getCustomerMap());
        return httpClientTemplateService.postTemplate(handlerHttpClientParam);
    }
}
