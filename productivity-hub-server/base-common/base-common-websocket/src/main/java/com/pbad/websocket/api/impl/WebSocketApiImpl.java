package com.pbad.websocket.api.impl;

import com.pbad.websocket.WebSocketManager;
import com.pbad.websocket.api.WebSocketApi;
import com.pbad.websocket.model.MessageDTO;
import com.pbad.websocket.utils.SpringContextUtil;
import org.springframework.stereotype.Service;

/**
 * ClassName: pushWebSocket
 * description: WebSocket消息推送接口实现类
 *
 * @author: pangdi
 * @date: 2023/4/21 14:09
 * @version: 1.0
 */
@Service
public class WebSocketApiImpl implements WebSocketApi {

    /**
     * 从Spring容器获取WebSocketManager
     */
    protected WebSocketManager getWebSocketManager() {
        return SpringContextUtil.getBean(WebSocketManager.class);
    }

    /**
     * 给单人推送消息.
     *
     * @param userAccount 用户账户
     * @param message     消息信息
     */
    @Override
    public void sendMessageToUser(String userAccount, String message) {
        MessageDTO messageDTO = new MessageDTO()
                .setUserAccount(userAccount)
                .setMessage(message);
        getWebSocketManager().sendMessageToUser(messageDTO);
    }

    /**
     * 给单人推送消息.
     * userAgent,message不能为空
     *
     * @param messageDTO 消息
     */
    @Override
    public synchronized void sendMessageToUser(MessageDTO messageDTO) {
        getWebSocketManager().sendMessageToUser(messageDTO);
    }

    /**
     * 给单人客户端推送消息.
     * userAgent,client,message不能为空
     *
     * @param messageDTO 消息
     */
    @Override
    public synchronized void sendMessageToClientUser(MessageDTO messageDTO) {
        getWebSocketManager().sendMessageToClientUser(messageDTO);
    }

    /**
     * 给多人推送消息.
     * manyUserAccount,client,message不能为空
     *
     * @param messageDTO 消息
     */
    @Override
    public synchronized void sendMessageToManyUser(MessageDTO messageDTO) {
        getWebSocketManager().sendMessageToManyUser(messageDTO);
    }

    /**
     * 消息群发
     *
     * message不能为空
     */
    @Override
    public synchronized void sendMessageToAll(MessageDTO messageDTO) {
        getWebSocketManager().sendMessageToAll(messageDTO);
    }

    /**
     * 消息群发.
     *
     * @param message     消息信息
     */
    @Override
    public void sendMessageToAll(String message) {
        MessageDTO messageDTO = new MessageDTO()
                .setMessage(message);
        getWebSocketManager().sendMessageToAll(messageDTO);
    }
}
