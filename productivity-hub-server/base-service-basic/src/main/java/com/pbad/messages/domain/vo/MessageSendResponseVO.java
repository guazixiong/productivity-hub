package com.pbad.messages.domain.vo;

import lombok.Data;

/**
 * 发送消息响应 VO.
 */
@Data
public class MessageSendResponseVO {
    private String requestId;
    private String status;
    private String detail;
}


