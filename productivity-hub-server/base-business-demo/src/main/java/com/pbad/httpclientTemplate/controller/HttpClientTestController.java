package com.pbad.httpclientTemplate.controller;

import com.pbad.httpclientTemplate.constant.CommonHttpClientMethodEnum;
import com.pbad.httpclientTemplate.httpClientDiyDemo.DiyHttpClientMethodEnum;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientProxyParam;
import com.pbad.httpclientTemplate.domain.HttpClientTemplateServiceEnum;
import com.pbad.httpclientTemplate.domain.TestUser;
import com.pbad.httpclientTemplate.proxy.HttpClientProxy;
import common.core.domain.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * 第三方调用api接口测试.
 *
 * @author: pangdi
 * @date: 2023/12/27 15:29
 * @version: 1.0
 */
@Api(tags = "第三方调用api接口测试")
@RestController
@RequestMapping("/http/client/template")
public class HttpClientTestController {

    private final HttpClientProxy httpClientProxy;

    @Autowired
    HttpClientTestController(HttpClientProxy httpClientProxy) {
        this.httpClientProxy = httpClientProxy;
    }

    /**
     * 通用模板用户生成
     */
    @ApiOperation(value = "用户生成")
    @PutMapping("/createUserCommonTemplate")
    public Response<Object> createUserCommonTemplate(@RequestBody TestUser testUser) {
        HandlerHttpClientProxyParam handlerHttpClientProxyParam = new HandlerHttpClientProxyParam()
                .setProxyTemplateBeanName(HttpClientTemplateServiceEnum.COMMON_HTTP_TEMPLATE.getBeanName())
                .setMethod(CommonHttpClientMethodEnum.CREATE_USER.getMethod())
                .setBusinessId(String.valueOf(System.currentTimeMillis()))
                .setParam(testUser)
                .setCustomerMap(new HashMap<>());
        return Response.ok(httpClientProxy.postJsonHttp(handlerHttpClientProxyParam));
    }

    /**
     * 自定义模板用户生成
     */
    @ApiOperation(value = "用户生成")
    @PutMapping("/createUserDiyTemplate")
    public Response<Object> createUserDiyTemplate(@RequestBody TestUser testUser) {
        HandlerHttpClientProxyParam handlerHttpClientProxyParam = new HandlerHttpClientProxyParam()
                .setProxyTemplateBeanName(HttpClientTemplateServiceEnum.DIY_HTTP_TEMPLATE.getBeanName())
                .setMethod(DiyHttpClientMethodEnum.CREATE_USER.getMethod())
                .setBusinessId(String.valueOf(System.currentTimeMillis()))
                .setParam(testUser)
                .setCustomerMap(new HashMap<>());
        return Response.ok(httpClientProxy.postJsonHttp(handlerHttpClientProxyParam));
    }

    /**
     * 上传图片请求模板
     */
    @ApiOperation(value = "上传图片")
    @PutMapping("/uploadPic")
    public Response<Object> uploadPic(@RequestBody TestUser testUser) {
        HandlerHttpClientProxyParam handlerHttpClientProxyParam = new HandlerHttpClientProxyParam()
                .setProxyTemplateBeanName(HttpClientTemplateServiceEnum.DIY_HTTP_TEMPLATE.getBeanName())
                .setMethod(DiyHttpClientMethodEnum.CREATE_USER.getMethod())
                .setBusinessId(String.valueOf(System.currentTimeMillis()))
                .setParam(testUser)
                .setCustomerMap(new HashMap<>());
        return Response.ok(httpClientProxy.postPicJsonHttp(handlerHttpClientProxyParam));
    }
}
