package com.pbad.websocket.utils;

import javax.websocket.Session;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * ClassName: WebSocketUtil
 * description: websocket工具类
 *
 * @author: pbad
 * @date: 2023/4/21 9:56
 * @version: 1.0
 */
public class WebSocketUtil {

    /**
     * 发送消息
     */
    public static Boolean sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步发送消息
     */
    public static Boolean sendMessageAsync(Session session, String message) {
        Future<Void> voidFuture = session.getAsyncRemote().sendText(message);
        return voidFuture.isDone();
    }

    /**
     * 发送字节消息
     */
    public static Boolean sendBytes(Session session, byte[] bytes) {
        try {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(bytes));
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步发送字节
     */
    public static Boolean sendBytesAsync(Session session, byte[] bytes) {
        Future<Void> voidFuture = session.getAsyncRemote().sendBinary(ByteBuffer.wrap(bytes));
        return voidFuture.isDone();
    }

    /**
     * 发送对象消息
     */
    public static Boolean sendObject(Session session, Object o) {
        try {
            session.getBasicRemote().sendObject(o);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步发送对象
     */
    public static Boolean sendObjectAsync(Session session, Object o) {
        Future<Void> voidFuture = session.getAsyncRemote().sendObject(o);
        return voidFuture.isDone();
    }
}
