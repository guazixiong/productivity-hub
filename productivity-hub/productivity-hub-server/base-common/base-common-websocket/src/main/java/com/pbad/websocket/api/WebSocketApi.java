package com.pbad.websocket.api;

import com.pbad.websocket.model.MessageDTO;
import org.springframework.scheduling.annotation.Async;

/**
 * ClassName: WebSocketApi
 * description: WebSocket消息推送接口
 *
 * @author: pbad
 * @date: 2023-9-11 10:30:20
 * @version: 1.0
 */
public interface WebSocketApi {

    /**
     * 给单人推送消息.
     *
     * @param userAccount 用户账户
     * @param message     消息信息
     */
    @Async
    void sendMessageToUser(String userAccount, String message);

    /**
     * 给单人推送消息.
     *
     * @param messageDTO 消息
     */
    @Async
    void sendMessageToUser(MessageDTO messageDTO);

    /**
     * 给单人客户端推送消息.
     *
     * @param messageDTO 消息
     */
    @Async
    void sendMessageToClientUser(MessageDTO messageDTO);

    /**
     * 给多人推送消息.
     *
     * @param messageDTO 消息
     */
    @Async
    void sendMessageToManyUser(MessageDTO messageDTO);

    /**
     * 消息群发.
     *
     * @param messageDTO 消息
     */
    @Async
    void sendMessageToAll(MessageDTO messageDTO);

    /**
     * 消息群发.
     *
     * @param message     消息信息
     */
    @Async
    void sendMessageToAll(String message);
}
