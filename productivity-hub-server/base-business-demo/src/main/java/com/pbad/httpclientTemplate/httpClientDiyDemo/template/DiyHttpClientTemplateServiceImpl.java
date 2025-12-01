package com.pbad.httpclientTemplate.httpClientDiyDemo.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pbad.httpclientTemplate.httpClientDiyDemo.DiyHttpClientMethodEnum;
import com.pbad.httpclientTemplate.domain.HandlerHttpClientParam;
import com.pbad.httpclientTemplate.domain.factory.HttpClientTemplateFactory;
import com.pbad.httpclientTemplate.service.template.HttpClientTemplateService;
import com.pbad.httpclientTemplate.util.HttpRequestUtil;
import common.exception.BusinessException;
import common.util.judge.JudgeParameterUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 自定义http请求模板接口实现类.
 *
 * @author: pangdi
 * @date: 2024-6-7 17:59:34
 * @version: 1.0
 */
@Service("diyHttpClientTemplateService")
@Transactional(rollbackFor = Exception.class)
public class DiyHttpClientTemplateServiceImpl implements HttpClientTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(DiyHttpClientTemplateServiceImpl.class);

    @Autowired
    private HttpClientTemplateFactory httpClientTemplateFactory;

    /**
     * post请求模板方法.
     *
     * @param handlerHttpClientParam 处理httpclient请求类
     * @return 返回结果
     */
    @Override
    public Object postTemplate(HandlerHttpClientParam handlerHttpClientParam) {
        logger.info("请求入参: " + handlerHttpClientParam.toString());
        String method = handlerHttpClientParam.getMethod();
        Object parm = handlerHttpClientParam.getParam();
        JudgeParameterUtil.checkNotNullOrEmpty(method, "502", "请求方法不能为空,请联系管理员进行处理");
        // 自定义传参
        Map<String, Object> customerMap = handlerHttpClientParam.getCustomerMap();
        logger.info("------------执行post请求方法" + method + "接口Start------------");

        DiyHttpClientMethodEnum httpClientMethodEnum = DiyHttpClientMethodEnum.getEnumByMethod(method);
        // 参数校验
        httpClientTemplateFactory.validParameter(httpClientMethodEnum.getHandlerBean(), parm);

        // 构造请求url
        String httpUrl = httpClientTemplateFactory.buildHttpUrl(httpClientMethodEnum.getHandlerBean(), parm);
        logger.info("获取请求url" + httpUrl);

        // 封装请求参数，bizContent（业务参数，需要加密）和check（需要生成密钥并加密）
        Map<String, String> reqMap = new HashMap<>();
        // 接口方法名，根据接口文档取值
        reqMap.put("method", DiyHttpClientMethodEnum.CREATE_USER.getMethod());
        reqMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
        // 固定utf-8
        reqMap.put("charset", "utf-8");
        reqMap.put("version", "1.0");

        // 生成对业务参数加密的密钥
        String key = "123456";
        // 加密后的密钥
        reqMap.put("check", key);

        // 封装业务参数,具体参数见文档
        logger.info("构造请求业务参数: ");
        Object bizContentMap = httpClientTemplateFactory.buildHttpContent(httpClientMethodEnum.getHandlerBean(), parm);
//        JSONObject bizJSONObj = JSONObject.parseObject(JSON.toJSONString(bizContentMap));
        reqMap.put("bizContent",JSON.toJSONString(bizContentMap));
        // 将请求参数进行sort排序，生成拼接的字符床，并使用接入方私钥对请求参数进行加签，并将加签的值存入请求参数sign中
        // 排序生成拼接字符串
        List<String> keys = new ArrayList<>(reqMap.keySet());
        Collections.sort(keys);
        StringBuilder sb1 = new StringBuilder();
        for (String k : keys) {
            if ("sign".equals(k)) {
                continue;
            }
            sb1.append(k).append("=");
            sb1.append(reqMap.get(k));
            sb1.append("&");
        }
        if (sb1.length() > 0) {
            sb1.setLength(sb1.length() - 1);
        }
        // 使用接入方私钥对排序的请求参数加签，并存放到请求参数里面.privateFilePath:私钥地址，privatePassword:私钥密钥
        String sign = "123456456789790";
        reqMap.put("sign", sign);

        // 发送http请求获取返回结果，请求地址以文档为准
        String result;
        try {
            result = HttpRequestUtil.getDoPostResponse(httpUrl, reqMap, "UTF-8", null, 20 * 1000);
            byte[] res = Base64.decodeBase64(result);
            Map<String, String> resMap = (Map<String, String>) JSONObject.parse(new String(res, "UTF-8"));
            System.out.println("http请求返回的结果为:" + JSONObject.toJSONString(resMap));

            // 对结果进行解密，
            // 针对自己的业务，做接入方的业务处理
            String businessData = "1245678798";
            resMap.put("decryptedResponseData", businessData);

            // 针对自己的业务，根据返回的结果的code 和 subCode 做接入方的业务处理
            Object buildResponse = httpClientTemplateFactory.buildResponse(httpClientMethodEnum.getHandlerBean(), resMap);
            logger.info("------------执行post请求方法" + method + "接口End------------");
            return buildResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("-100", e.getMessage());
        }
    }

    @Override
    public Object getTemplate(HandlerHttpClientParam handlerHttpClientParam) {
        return null;
    }
}
