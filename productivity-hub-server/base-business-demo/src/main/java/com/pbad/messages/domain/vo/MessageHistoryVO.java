package com.pbad.messages.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 消息推送历史 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class MessageHistoryVO {
    /**
     * 历史记录ID
     */
    private String id;

    /**
     * 推送渠道
     */
    private String channel;

    /**
     * 原始请求参数
     */
    private Map<String, Object> request;

    /**
     * 发送状态
     */
    private String status;

    /**
     * 第三方服务响应内容
     */
    private String response;

    /**
     * 创建时间（YYYY-MM-DD HH:mm）
     */
    private String createdAt;
}

