package com.pbad.messages.domain.dto;

import lombok.Data;

import java.util.Map;

/**
 * 发送消息请求 DTO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class MessageSendDTO {
    /**
     * 推送渠道（sendgrid, dingtalk）
     */
    private String channel;

    /**
     * 消息数据（根据渠道不同，包含不同的字段）
     */
    private Map<String, Object> data;
}

