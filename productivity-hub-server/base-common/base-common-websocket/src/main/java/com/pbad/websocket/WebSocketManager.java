package com.pbad.websocket;

import com.pbad.websocket.common.WebSocketMessageTypeEnum;
import com.pbad.websocket.model.MessageDTO;
import com.pbad.websocket.model.WebSocket;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: WebSocketManager
 * description: websocket管理
 *
 * @author: pangdi
 * @date: 2023/4/21 9:30
 * @version: 1.0
 */
public interface WebSocketManager {

    /**
     * 在容器中的名字
     */
    String WEBSOCKET_MANAGER_NAME = "webSocketManager";

    /**
     * 根据标识获取websocket session.
     *
     * @param identifier 标识
     * @return WebSocket
     */
    WebSocket get(String identifier);

    /**
     * 同一用户多点登录时使用.
     *
     * @param userAccount 用户账户
     * @return WebSocket
     */
    List<WebSocket> getList(String userAccount);

    /**
     * 放入一个 com.sangto.websocket session.
     *
     * @param identifier 标识
     * @param webSocket  com.sangto.websocket
     */
    void put(String identifier, WebSocket webSocket);

    /**
     * 删除.
     *
     * @param identifier 标识
     */
    void remove(String identifier);

    /**
     * 获取当前机器上的保存的WebSocket.
     *
     * @return WebSocket Map
     */
    Map<String, WebSocket> localWebSocketMap();

    /**
     * 统计当前实例在线人数,如果不允许一个账号多次登录,默认实现就可以,如果一个人多次登录,需要重新该方法.
     *
     * @return 统计当前实例在线人数
     */
    default int size() {
        return localWebSocketMap().size();
    }

    /**
     * 统计当前实例在线用户
     *
     * @return 在线用户id
     */
    default List<String> getOnlineUsers() {
        return localWebSocketMap().values().stream().map(WebSocket::getUserAccount).collect(Collectors.toList());
    }

    /**
     * 给某人发送消息.
     *
     * @param messageDto 推送消息类
     */
    void sendMessageToUser(MessageDTO messageDto);

    /**
     * 给某人某个客户端发送消息.
     *
     * @param messageDto 推送消息类
     */
    void sendMessageToClientUser(MessageDTO messageDto);

    /**
     * 给多个人发送消息.
     *
     * @param messageDto 推送消息类
     */
    void sendMessageToManyUser(MessageDTO messageDto);

    /**
     * 群发消息
     *
     * @param messageDto 推送消息类
     */
    void sendMessageToAll(MessageDTO messageDto);

    /**
     * WebSocket接收到消息的函数调用.
     *
     * @param identifier 标识
     * @param message    消息内容
     */
    void onMessage(String identifier, String message);

    /**
     * 在OnMessage中判断是否是心跳,
     * 从客户端的消息判断是否是ping消息.
     *
     * @param identifier 标识
     * @param message    消息
     * @return 是否是ping消息
     */
    default Boolean isPing(String identifier, String message) {
        return WebSocketMessageTypeEnum.PING.getValue().equalsIgnoreCase(message);
    }

    /**
     * 返回心跳信息.
     *
     * @param identifier 标识
     * @param message    消息
     * @return 返回的pong消息
     */
    default String pong(String identifier, String message) {
        return WebSocketMessageTypeEnum.PONG.getValue();
    }
}
