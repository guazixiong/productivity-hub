package com.pbad.messages.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 消息推送历史 VO.
 */
@Data
public class MessageHistoryVO {
    private String id;
    private String channel;
    private Map<String, Object> request;
    private String status;
    private String response;
    private String createdAt;
}


