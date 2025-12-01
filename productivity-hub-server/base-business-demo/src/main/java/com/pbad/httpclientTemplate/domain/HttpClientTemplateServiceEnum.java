package com.pbad.httpclientTemplate.domain;

import common.exception.BusinessException;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * http远程请求模板服务枚举类
 *
 * @author: pangdi
 * @date: 2024-6-14 11:36:11
 * @version: 1.0
 */
public enum HttpClientTemplateServiceEnum {
    /**
     * 默认http请求模板
     */
    COMMON_HTTP_TEMPLATE("默认http请求模板", "commonHttpClientTemplateService"),

    /**
     * 自定义http请求模板
     */
    DIY_HTTP_TEMPLATE("自定义http请求模板", "diyHttpClientTemplateService"),
    ;

    /**
     * 说明
     */
    private final String msg;

    /**
     * 处理bean
     */
    private final String beanName;

    /**
     * 通过方法获取枚举类.
     *
     * @param beanName bean名称
     * @return 枚举类
     */
    public static HttpClientTemplateServiceEnum getEnumByBean(String beanName) {
        HttpClientTemplateServiceEnum[] values = HttpClientTemplateServiceEnum.values();
        List<HttpClientTemplateServiceEnum> collect = Arrays.stream(values).filter(e -> e.getBeanName().equals(beanName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            throw new BusinessException("501", "获取远程请求模板异常,请联系管理员进行处理");
        }
        return collect.get(0);
    }

    HttpClientTemplateServiceEnum(String msg, String beanName) {
        this.msg = msg;
        this.beanName = beanName;
    }

    public String getMsg() {
        return msg;
    }

    public String getBeanName() {
        return beanName;
    }
}
