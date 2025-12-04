package com.pbad.auth.domain.vo;

import lombok.Data;

/**
 * 重置密码响应 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class ResetPasswordResponseVO {
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;
}

