package com.pbad.httpclientTemplate.httpClientDiyDemo.service;

import com.pbad.httpclientTemplate.httpClientDiyDemo.DiyHttpClientMethodEnum;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientResponse;
import com.pbad.httpclientTemplate.service.HttpClientHandlerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义请求模板创建用户 http请求调用接口实现类
 *
 * @author: pangdi
 * @date: 2024-6-7 18:03:57
 * @version: 1.0
 */
@Service("diyCreateUserHandlerService")
@Transactional(rollbackFor = Exception.class)
public class DiyCreateUserHandlerServiceImpl implements HttpClientHandlerService {

    /**
     * 校验参数
     *
     * @param object 请求参数
     */
    @Override
    public void validParameter(Object object) {
    }

    /**
     * 构建http请求url
     *
     * @param object 请求参数
     * @return url
     */
    @Override
    public String buildHttpUrl(Object object) {
        return DiyHttpClientMethodEnum.CREATE_USER.getHttpUrl();
    }

    /**
     * 构建http请求内容
     *
     * @param object 请求参数
     * @return http请求内容
     */
    @Override
    public Object buildHttpContent(Object object) {
        Map<String, Object> bizContentMap = new HashMap<>();
        return bizContentMap;
    }

    /**
     * 构建返回结果
     *
     * @param result 返回结果
     * @return 返回结果
     */
    @Override
    public Object buildResponse(Object result) {
        return HandlerHttpClientResponse.ok(result, result);
    }
}
