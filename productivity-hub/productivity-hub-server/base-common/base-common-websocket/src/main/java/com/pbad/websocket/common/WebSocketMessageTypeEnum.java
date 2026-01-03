package com.pbad.websocket.common;

/**
 * ClassName: WebSocketMessageTypeEnum
 * description: WebSocket枚举类
 *
 * @author: pbad
 * @date: 2023/4/21 9:30
 * @version: 1.0
 */
public enum WebSocketMessageTypeEnum {

    /**
     * ping
     */
	PING("ping"),
    /**
     * ping
     */
    PONG("pong"),
    /**
     * 启动连接
     */
    EVENT_TYPE_OPEN("open"),
    /**
     * 关闭连接
     */
    EVENT_TYPE_CLOSE("close")
    ;

	private String value;

    public String getValue() {
        return value;
    }

    WebSocketMessageTypeEnum(String value) {
        this.value = value;
    }

}
