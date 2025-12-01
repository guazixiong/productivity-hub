package com.pbad.websocket.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Date;

/**
 * ClassName: websocket
 * description: websocket 连接参数
 *
 * @author: pangdi
 * @date: 2023/4/21 9:30
 * @version: 1.0
 */
@Api("websocket信息类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain=true)
public class WebSocket {

    /**
     * 代表一个连接
     */
    @ApiModelProperty("请求连接")
    private Session session;

    /**
     * 用户id
     */
    @ApiModelProperty("用户id")
    private String userAccount;

    /**
     * 客户机：PC，APP
     */
    @ApiModelProperty("客户机：PC，APP")
    private String client;

    /**
     * 唯一标识
     */
    @ApiModelProperty("唯一标识")
    private String identifier;

    /**
     * 最后心跳时间
     */
    @ApiModelProperty("最后心跳时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHeart;

    public void closeSession() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException ignored) {
        }
    }
}
