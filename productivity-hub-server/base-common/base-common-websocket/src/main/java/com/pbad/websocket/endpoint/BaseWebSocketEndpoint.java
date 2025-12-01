package com.pbad.websocket.endpoint;

import com.pbad.websocket.WebSocketManager;
import com.pbad.websocket.model.WebSocket;
import com.pbad.websocket.utils.SpringContextUtil;
import com.pbad.websocket.utils.WebSocketUtil;
import common.util.judge.JudgeParameterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.Date;

/**
 * ClassName: BaseWebSocketEndpoint
 * description:websocketEndpoint 基础类
 *
 * @author: pangdi
 * @date: 2023/4/21 17:17
 * @version: 1.0
 */
public class BaseWebSocketEndpoint {

    protected static final Logger logger = LoggerFactory.getLogger(BaseWebSocketEndpoint.class);

    private static final String UNDERLINE = "_";

    /**
     * 从Spring容器获取WebSocketManager
     */
    protected WebSocketManager getWebSocketManager() {
        return SpringContextUtil.getBean(WebSocketManager.class);
    }

    /**
     * 添加用户.
     *
     * @param identifier 用户唯一标识
     * @param session    session会话
     * @param client     客户端名称
     */
    public void connect(String identifier, Session session, String client) {
        if (JudgeParameterUtil.isNullOrEmpty(identifier)) {
            return;
        }
        try {
            WebSocketManager webSocketManager = getWebSocketManager();
            WebSocket webSocket = new WebSocket()
                    .setIdentifier(identifier)
                    .setSession(session)
                    .setUserAccount(identifier)
                    .setClient(client)
                    .setLastHeart(new Date());
            webSocketManager.put(client + UNDERLINE + identifier, webSocket);
            logger.info("连接成功,session:{}", session);
        } catch (Exception e) {
            logger.info("连接失败,session:{}", session);
            // 移出用户
            disconnect(identifier);
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 移出用户.
     *
     * @param identifier 用户唯一标识
     */
    public void disconnect(String identifier) {
        getWebSocketManager().remove(identifier);
    }

    /**
     * 更新心跳时间
     */
    public void receiveMessage(String identifier, String message, Session session) {
        WebSocketManager webSocketManager = getWebSocketManager();
        //监测
        if (Boolean.TRUE.equals(webSocketManager.isPing(identifier, message))) {
            String pong = webSocketManager.pong(identifier, message);
            WebSocket webSocket = webSocketManager.get(identifier);
            //更新心跳时间
            if (null != webSocket) {
                webSocket.setLastHeart(new Date());
                WebSocketUtil.sendMessageAsync(session, pong);
            } else {
                WebSocketUtil.sendMessageAsync(session, "连接失败");
            }
            return;
        }
        //收到其他消息的时候
        webSocketManager.onMessage(identifier, message);
    }
}
