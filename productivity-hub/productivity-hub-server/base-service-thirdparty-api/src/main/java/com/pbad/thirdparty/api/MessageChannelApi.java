package com.pbad.thirdparty.api;

import java.util.Map;

/**
 * 消息推送渠道API接口.
 * <p>提供消息推送功能，不依赖数据库，所有配置参数由调用方传入.</p>
 *
 * @author pbad
 */
public interface MessageChannelApi {

    /**
     * 发送消息.
     *
     * @param data    消息数据
     * @param configs 调用所需的第三方配置（由业务侧传入）
     * @return 发送结果（成功返回响应内容，失败抛异常）
     */
    String sendMessage(Map<String, Object> data, Map<String, String> configs);
}

