package com.pbad.httpclientTemplate.service.template.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.httpclientTemplate.domain.CommonHttpClientMethodEnum;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientParam;
import com.pbad.httpclientTemplate.domain.SSLClient;
import com.pbad.httpclientTemplate.domain.factory.HttpClientTemplateFactory;
import com.pbad.httpclientTemplate.service.template.HttpClientTemplateService;
import common.util.judge.JudgeParameterUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

/**
 * http请求模板接口实现类.
 *
 * @author: pangdi
 * @date: 2023/12/27 16:44
 * @version: 1.0
 */
@Service("commonHttpClientTemplateService")
public class CommonHttpClientTemplateServiceImpl implements HttpClientTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(CommonHttpClientTemplateServiceImpl.class);

    @Autowired
    private HttpClientTemplateFactory httpClientTemplateFactory;

    /**
     * 创建httpclient.
     *
     * @return httpclient
     */
    private CloseableHttpClient createHttpClient() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = new SSLClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpClient;
    }

    /**
     * post请求模板方法.
     *
     * @param handlerHttpClientParam 处理httpclient请求类
     * @return 返回结果
     */
    @Override
    public Object postTemplate(HandlerHttpClientParam handlerHttpClientParam) {
        logger.info("请求入参: " + handlerHttpClientParam.toString());
        // 请求方法
        String method = handlerHttpClientParam.getMethod();
        // 请求入参
        Object data = handlerHttpClientParam.getParam();
        // 业务id(唯一性id)
        String businessId = handlerHttpClientParam.getBusinessId();
        JudgeParameterUtil.checkNotNullOrEmpty(method, "502", "请求方法不能为空,请联系管理员进行处理");
        logger.info("校验入参: " + data.toString());
        logger.info("------------执行post请求方法" + method + "接口Start------------");
        CommonHttpClientMethodEnum httpClientMethodEnum = CommonHttpClientMethodEnum.getEnumByMethod(method);
        // 参数校验
        logger.info("构造请求url");
        httpClientTemplateFactory.validParameter(httpClientMethodEnum.getHandlerBean(), data);
        CloseableHttpClient httpClient = createHttpClient();
        String url = httpClientTemplateFactory.buildHttpUrl(httpClientMethodEnum.getHandlerBean(), data);
        logger.info("构造请求url: " + url);
        HttpPost httpPost = new HttpPost(url);
        // 构造请求header
        logger.info("构造请求header: ");
        Map<String, String> headerMap = httpClientTemplateFactory.buildHttpHeaders(httpClientMethodEnum.getHandlerBean(), data);
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach(httpPost::setHeader);
        }
        logger.info("构造请求header: " + data.toString());
        // 构造请求body
        logger.info("构造请求body: ");
        Object body = httpClientTemplateFactory.buildHttpContent(httpClientMethodEnum.getHandlerBean(), data);
        String json = JSON.toJSONString(body);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        logger.info("构造请求body: " + json);
        // 执行post请求
        logger.info("执行post请求");
        CloseableHttpResponse response = null;
        String result = null;
        try {
            response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("请求结果: " + result);
        // 校验返回结果
        logger.info("校验返回结果");
        httpClientTemplateFactory.validResponse(httpClientMethodEnum.getHandlerBean(), result);
        // 构造返回结果
        logger.info("构造返回结果");
        Object buildResponse = httpClientTemplateFactory.buildResponse(method, result);
        logger.info("------------执行post请求方法" + method + "接口End------------");
        return buildResponse;
    }

    /**
     * get请求模板方法.
     *
     * @param handlerHttpClientParam 处理httpclient请求类
     * @return 返回结果
     */
    @Override
    public Object getTemplate(HandlerHttpClientParam handlerHttpClientParam) {
        logger.info("请求入参: " + handlerHttpClientParam.toString());
        String method = handlerHttpClientParam.getMethod();
        Object data = handlerHttpClientParam.getParam();
        JudgeParameterUtil.checkNotNullOrEmpty(method, "502", "请求方法不能为空,请联系管理员进行处理");
        // 自定义传参
        Map<String, Object> customerMap = handlerHttpClientParam.getCustomerMap();
        logger.info("------------执行get请求方法" + method + "接口Start------------");
        // 参数校验
        logger.info("校验入参: " + data.toString());
        httpClientTemplateFactory.validParameter(method, data);
        // 构造请求url
        logger.info("构造请求url: ");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = httpClientTemplateFactory.buildHttpUrl(method, data);
        logger.info("请求url: " + url);
        HttpGet httpGet = new HttpGet(url);
        // 构造请求header
        logger.info("构造请求header: ");
        Map<String, String> headerMap = httpClientTemplateFactory.buildHttpHeaders(method, data);
        if (!CollectionUtils.isEmpty(headerMap)) {
            headerMap.forEach(httpGet::setHeader);
        }
        logger.info("请求header: " + data.toString());
        // 执行get请求
        logger.info("执行get请求");
        CloseableHttpResponse response;
        String result = null;
        try {
            response = httpClient.execute(httpGet);
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.info("HTTP请求异常：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        logger.info("请求结果: " + result);
        // 校验返回结果
        logger.info("校验返回结果");
        httpClientTemplateFactory.validResponse(method, result);
        // 构造返回结果
        logger.info("构造返回结果");
        Object buildResponse = httpClientTemplateFactory.buildResponse(method, result);
        logger.info("------------执行get请求方法" + method + "接口End------------");
        return buildResponse;
    }
}
