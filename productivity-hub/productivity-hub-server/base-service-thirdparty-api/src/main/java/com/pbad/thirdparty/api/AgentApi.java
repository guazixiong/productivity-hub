package com.pbad.thirdparty.api;

import java.util.Map;

/**
 * 智能体调用API接口.
 * <p>提供智能体调用功能，不依赖数据库，所有参数由调用方传入.</p>
 *
 * @author pbad
 */
public interface AgentApi {

    /**
     * 调用智能体.
     *
     * @param agentId   智能体ID
     * @param agentName 智能体名称
     * @param input     输入数据
     * @param configs   调用所需的第三方配置（由业务侧传入）
     * @return 智能体响应结果
     */
    String invokeAgent(String agentId, String agentName, Map<String, Object> input, Map<String, String> configs);
}

