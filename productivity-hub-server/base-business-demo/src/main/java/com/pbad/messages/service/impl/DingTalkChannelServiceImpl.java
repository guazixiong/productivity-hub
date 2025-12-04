package com.pbad.messages.service.impl;

import com.pbad.messages.service.MessageChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 钉钉 Webhook 推送服务实现.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service("dingtalkChannelService")
public class DingTalkChannelServiceImpl implements MessageChannelService {

    @Override
    public String sendMessage(Map<String, Object> data) {
        // TODO: 实际生产环境需要调用钉钉 Webhook API
        // 这里仅做模拟实现
        String webhook = (String) data.get("webhook");
        String msgType = (String) data.get("msgType");
        String content = (String) data.get("content");

        log.info("钉钉发送消息: webhook={}, msgType={}", webhook, msgType);

        // 模拟发送成功
        return "DingTalk message sent successfully";
    }
}

