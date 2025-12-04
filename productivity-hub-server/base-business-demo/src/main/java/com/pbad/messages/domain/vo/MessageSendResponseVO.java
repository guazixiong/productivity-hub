package com.pbad.messages.domain.vo;

import lombok.Data;

/**
 * 发送消息响应 VO.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Data
public class MessageSendResponseVO {
    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 状态（success, queued, failed）
     */
    private String status;

    /**
     * 详情
     */
    private String detail;
}

