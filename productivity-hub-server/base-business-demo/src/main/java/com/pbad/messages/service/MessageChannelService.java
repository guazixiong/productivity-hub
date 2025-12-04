package com.pbad.messages.service;

import java.util.Map;

/**
 * 消息推送渠道服务接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface MessageChannelService {

    /**
     * 发送消息
     *
     * @param data 消息数据
     * @return 发送结果（成功返回响应内容，失败返回错误信息）
     */
    String sendMessage(Map<String, Object> data);
}

