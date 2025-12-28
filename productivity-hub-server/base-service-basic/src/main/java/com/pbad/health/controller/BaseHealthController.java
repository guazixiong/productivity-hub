package com.pbad.health.controller;

import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import org.springframework.util.StringUtils;

/**
 * 健康模块基础Controller.
 * 提供公共方法，减少代码重复.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public abstract class BaseHealthController {

    /**
     * 获取当前用户ID.
     * 如果用户未登录，抛出401异常.
     *
     * @return 当前用户ID
     * @throws BusinessException 如果用户未登录
     */
    protected String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "未登录或登录已过期");
        }
        return userId;
    }
}

