package com.pbad.httpclientTemplate.constant;

import common.exception.BusinessException;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共http请求方法枚举类
 *
 * @author: pangdi
 * @date: 2023-12-27 15:51:02
 * @version: 1.0
 */
public enum CommonHttpClientMethodEnum {

    /**
     * 创建用户
     */
    CREATE_USER("创建用户", "creatUser", "http://127.0.0.1/createUser", "commonCreateUserHandlerService"),
    ;

    /**
     * 说明
     */
    private final String msg;

    /**
     * 方法名
     */
    private final String method;

    /**
     * 测试环境请求地址
     */
    private final String httpUrl;

    /**
     * 处理bean
     */
    private final String handlerBean;

    /**
     * 通过方法获取枚举类.
     *
     * @param method 方法名
     * @return 枚举类
     */
    public static CommonHttpClientMethodEnum getEnumByMethod(String method) {
        CommonHttpClientMethodEnum[] values = CommonHttpClientMethodEnum.values();
        List<CommonHttpClientMethodEnum> collect = Arrays.stream(values).filter(e -> e.getMethod().equals(method)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            throw new BusinessException("501", "获取请求方法url异常,请联系管理员进行处理");
        }
        return collect.get(0);
    }

    CommonHttpClientMethodEnum(String msg, String method, String httpUrl, String handlerBean) {
        this.msg = msg;
        this.method = method;
        this.httpUrl = httpUrl;
        this.handlerBean = handlerBean;
    }

    public String getMsg() {
        return msg;
    }

    public String getMethod() {
        return method;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public String getHandlerBean() {
        return handlerBean;
    }
}
