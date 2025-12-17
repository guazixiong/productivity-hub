package com.pbad.messages.domain.po;

import lombok.Data;

/**
 * 消息推送历史实体类（PO）.
 */
@Data
public class MessageHistoryPO {
    private String id;
    private String userId;
    private String channel;
    private String requestData;
    private String status;
    private String responseData;
    private String createdAt;
}


