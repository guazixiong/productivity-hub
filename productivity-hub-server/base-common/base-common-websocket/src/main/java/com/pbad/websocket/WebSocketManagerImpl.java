package com.pbad.websocket;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.pbad.websocket.exception.WebSocketExceptionMsgEnum;
import com.pbad.websocket.model.MessageDTO;
import com.pbad.websocket.model.WebSocket;
import com.pbad.websocket.utils.WebSocketUtil;
import common.util.judge.JudgeParameterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ClassName: WebSocketManagerImpl
 * description: WebSocketManager实现类
 *
 * @author: pangdi
 * @date: 2023/4/21 9:30
 * @version: 1.0
 */
@Service
public class WebSocketManagerImpl implements WebSocketManager {

    /**
     * 日志控制器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketManagerImpl.class);

    /**
     * 全局只有一个 WebSocketManager ，才可以定义为非static
     */
    private final Map<String, WebSocket> connections = new ConcurrentHashMap<>(100);

    /**
     * 存储连接用户值,在put,remove,close时增加自定义操作
     */
    private final Map<String, Set<String>> accountIdentifyMap = new ConcurrentHashMap<>(100);

    /**
     * 同一用户 单点单点登录时使用
     *
     * @param identifier 标识
     * @return websocket对象
     */
    @Override
    public WebSocket get(String identifier) {
        return connections.get(identifier);
    }

    /**
     * 通过用户标识查询用户
     *
     * @param userAccount 标识
     * @return websocket对象集合
     */
    @Override
    public List<WebSocket> getList(String userAccount) {
        return getConnectionForUser(userAccount);
    }

    /**
     * 连接websocket session
     *
     * @param identifier 标识
     * @param webSocket  com.sangto.websocket
     */
    @Override
    public void put(String identifier, WebSocket webSocket) {
        connections.put(identifier, webSocket);
        Set<String> set = accountIdentifyMap.getOrDefault(webSocket.getUserAccount(), Collections.synchronizedSet(new HashSet<>()));
        set.add(identifier);
        accountIdentifyMap.putIfAbsent(webSocket.getUserAccount(), set);
        //TODO 发送自定义连接事件
    }


    /**
     * 删除
     *
     * @param identifier 标识
     */
    @Override
    public void remove(String identifier) {
        WebSocket removedWebSocket = connections.remove(identifier);
        if (removedWebSocket == null) {
            return;
        }
        accountIdentifyMap.get(removedWebSocket.getUserAccount()).remove(identifier);
        // TODO 自定义操作
        // 关闭session连接
        removedWebSocket.closeSession();
    }

    /**
     * 获取当前机器上的保存的WebSocket
     *
     * @return WebSocket Map
     */
    @Override
    public Map<String, WebSocket> localWebSocketMap() {
        return Collections.unmodifiableMap(connections);
    }

    /**
     * 统计当前实例在线人数,如果不允许一个账号多次登录,默认实现就可以,如果一个人多次登录,需要重新该方法
     *
     * @return 统计当前实例在线人数
     */
    @Override
    public int size() {
        return (int) Collections.unmodifiableMap(accountIdentifyMap)
                .entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .count();
    }

    /**
     * 给某人发送消息
     *
     * @param messageDTO 消息推送类
     */
    @Override
    public void sendMessageToUser(MessageDTO messageDTO) {
        JudgeParameterUtil.checkNotNull(messageDTO.getUserAccount(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorMessage());
        JudgeParameterUtil.checkNotNull(messageDTO.getMessage(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorMessage());
        String message = JSON.toJSONString(messageDTO);
        List<WebSocket> list = getList(messageDTO.getUserAccount());
        if (CollUtil.isEmpty(list)) {
            LOGGER.info("用户id:  - {}已经离线", messageDTO.getUserAccount());
        }
        list.forEach(e -> WebSocketUtil.sendMessageAsync(e.getSession(), message));
    }

    /**
     * 给某人某个客户端发送消息
     *
     * @param messageDTO 消息推送类
     */
    @Override
    public void sendMessageToClientUser(MessageDTO messageDTO) {
        JudgeParameterUtil.checkNotNull(messageDTO.getUserAccount(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorMessage());
        JudgeParameterUtil.checkNotNull(messageDTO.getMessage(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorMessage());
        JudgeParameterUtil.checkNotNull(messageDTO.getClient(),
                WebSocketExceptionMsgEnum.CLIENT_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.CLIENT_IS_NULL.getErrorMessage());
        String message = JSON.toJSONString(messageDTO);
        List<WebSocket> list = getList(messageDTO.getUserAccount());
        if (CollUtil.isEmpty(list)) {
            LOGGER.info("用户id:  - {}已经离线", messageDTO.getUserAccount());
        }
        list.forEach(e -> {
            if (e.getClient().toLowerCase(Locale.ROOT).equals(messageDTO.getClient().toLowerCase(Locale.ROOT))) {
                WebSocketUtil.sendMessageAsync(e.getSession(), message);
            }
        });
    }

    /**
     * 给多个人发送消息.
     *
     * @param messageDTO 推送消息类
     */
    @Override
    public void sendMessageToManyUser(MessageDTO messageDTO) {
        JudgeParameterUtil.checkNotNull(messageDTO.getUserAccount(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.USER_ACCOUNT_IS_NULL.getErrorMessage());
        JudgeParameterUtil.checkNotNull(messageDTO.getMessage(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorMessage());
        String message = JSON.toJSONString(messageDTO);
        messageDTO.getManyUserAccount()
                .forEach(e -> {
                    List<WebSocket> list = getList(e);
                    if (CollUtil.isEmpty(list)) {
                        LOGGER.info("用户id:  - {}已经离线", e);
                    }
                    //本地能找到就直接发
                    list.forEach(webSocket -> {
                        if (webSocket.getClient().toLowerCase(Locale.ROOT).equals(messageDTO.getClient().toLowerCase(Locale.ROOT))) {
                            WebSocketUtil.sendMessageAsync(webSocket.getSession(), message);
                        }
                    });
                });
    }

    /**
     * 群发消息.
     *
     * @param messageDTO 消息推送类
     */
    @Override
    public void sendMessageToAll(MessageDTO messageDTO) {
        JudgeParameterUtil.checkNotNull(messageDTO.getMessage(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorCode(),
                WebSocketExceptionMsgEnum.MESSAGE_IS_NULL.getErrorMessage());
        List<WebSocket> webSockets = accountIdentifyMap.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet())
                .stream()
                .map(connections::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // 本地能找到就直接发
        if (CollUtil.isNotEmpty(webSockets)) {
            String message = JSON.toJSONString(messageDTO);
            webSockets.forEach(e -> WebSocketUtil.sendMessageAsync(e.getSession(), message));
        }
    }

    /**
     * WebSocket接收到消息的函数调用
     *
     * @param identifier 标识
     * @param message    消息内容
     */
    @Override
    public void onMessage(String identifier, String message) {
        WebSocket webSocket = connections.get(identifier);
        //发布一下消息事件,让关注该事件的人去处理
        if (webSocket != null) {
            // TODO 自定义事件
        }
    }

    /**
     * 通过用户标识查询用户
     *
     * @param userAccount 标识
     * @return 用户websocket连接信息
     */
    private List<WebSocket> getConnectionForUser(String userAccount) {
        return accountIdentifyMap.getOrDefault(userAccount, new HashSet<>())
                .stream()
                .map(connections::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
