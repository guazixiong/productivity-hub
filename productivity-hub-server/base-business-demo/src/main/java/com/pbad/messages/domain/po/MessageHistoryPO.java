package com.pbad.messages.domain.po;

import lombok.Data;

/**
 * 消息推送历史实体类（PO）.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class MessageHistoryPO {
    /**
     * 历史记录ID
     */
    private String id;

    /**
     * 推送渠道（sendgrid, dingtalk）
     */
    private String channel;

    /**
     * 原始请求参数（JSON）
     */
    private String requestData;

    /**
     * 发送状态（success, failed）
     */
    private String status;

    /**
     * 第三方服务响应内容
     */
    private String responseData;

    /**
     * 创建时间（YYYY-MM-DD HH:mm）
     */
    private String createdAt;
}

