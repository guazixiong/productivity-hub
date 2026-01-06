package com.pbad.websocket.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * ClassName: WebSocketEndpoint
 * description: websocket监听类.
 *
 * @author: pbad
 * @date: 2023/4/21 9:56
 * @version: 1.0
 */
@Component
@ServerEndpoint(value = "/socketServer/{client}/{identifier}")
public class WebSocketEndpoint extends BaseWebSocketEndpoint {

    /**
     * 日志控制器
     */
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEndpoint.class);

    private static final String UNDERLINE = "_";

    /**
     * 连接建立成功调用的方法.
     *
     * @param session    session 对象
     * @param identifier 用户唯一标识
     * @param client     客户端
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("identifier") String identifier, @PathParam("client") String client) {
        try {
            logger.info("用户id: {}建立连接 ", identifier);
            connect(identifier, session, client);
        } catch (Exception ex) {
            logger.info("用户id: {}连接异常", identifier);
            logger.info("异常信息 - {}", ex);
        }
    }

    /**
     * 接收消息调用方法.
     *
     * @param message    数据源
     * @param session    session对象
     * @param identifier 用户唯一标识
     * @param client     客户端
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("identifier") String identifier, @PathParam("client") String client) {
        receiveMessage(client + UNDERLINE + identifier, message, session);
    }

    /**
     * 连接关闭时.
     *
     * @param session    session对象
     * @param identifier 用户唯一标识
     * @param client     客户端
     */
    @OnClose
    public void onClose(Session session, @PathParam("identifier") String identifier, @PathParam("client") String client) throws Exception {
        logger.info("用户id: {}退出连接", identifier);
        disconnect(client + UNDERLINE + identifier);
    }

    /**
     * 抛出异常时处理.
     *
     * @param t          异常
     * @param identifier 用户唯一标识
     * @param client     客户端
     */
    @OnError
    public void onError(Throwable t, @PathParam("identifier") String identifier, @PathParam("client") String client) throws Exception {
        logger.info("用户id: {}连接异常", identifier);
        logger.error("异常信息 - {}", t);
        // 移出用户
        disconnect(client + UNDERLINE + identifier);
    }
}
