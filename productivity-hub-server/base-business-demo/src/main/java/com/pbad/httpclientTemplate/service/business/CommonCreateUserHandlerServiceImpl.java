package com.pbad.httpclientTemplate.service.business;

import com.pbad.httpclientTemplate.domain.CommonHttpClientMethodEnum;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientResponse;
import com.pbad.httpclientTemplate.service.HttpClientHandlerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用模板用户生成 http请求调用接口实现类
 *
 * @author: pangdi
 * @date: 2023/12/27 17:52
 * @version: 1.0
 */
@Service("creatUserHandlerService")
@Transactional(rollbackFor = Exception.class)
public class CommonCreateUserHandlerServiceImpl implements HttpClientHandlerService {

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
        return CommonHttpClientMethodEnum.CREATE_USER.getHttpUrl();
    }

    /**
     * 构建http请求头
     *
     * @param object 请求参数
     * @return http请求头
     */
    @Override
    public Map<String, String> buildHttpHeaders(Object object) {
        return Stream.of(new String[][]{
                {"Content-Type", "application/json"},
                {"Accept", "application/json"},
                {"Authorization", "Bearer 123456"},
                {"Accept-Charset", "UTF-8"},
                {"Accept-Encoding", "gzip, deflate"},
                {"Connection", "keep-alive"}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
    }

    /**
     * 构建http请求内容
     *
     * @param object 请求参数
     * @return http请求内容
     */
    @Override
    public Object buildHttpContent(Object object) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("name", "1111");
        map.put("sex", "0");
        return map;
    }

    /**
     * 校验返回结果
     *
     * @param result 返回结果
     */
    @Override
    public void validResponse(String result) {

    }

    @Override
    public Object buildResponse(Object result) {
        String data = (String) result;
        HandlerHttpClientResponse response = new HandlerHttpClientResponse();
        response.setResult(result);
        return response;
    }
}
