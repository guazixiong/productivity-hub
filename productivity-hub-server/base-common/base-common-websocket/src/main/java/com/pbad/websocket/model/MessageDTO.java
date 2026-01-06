package com.pbad.websocket.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * ClassName: MessageDto
 * description: 消息推送类
 *
 * @author: pbad
 * @date: 2023-9-11 10:31:37
 * @version: 1.0
 */
@Api("消息推送类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageDTO {
    /**
     * 消息类型
     */
    @ApiModelProperty("消息类型")
    private String type;

    /**
     * 消息信息
     */
    @ApiModelProperty("消息信息")
    private String message;

    /**
     * 用户账户(单个账户推送)
     */
    @ApiModelProperty("用户账户(单个账户推送)")
    private String userAccount;

    /**
     * 客户端
     */
    @ApiModelProperty("客户端")
    private String client;

    /**
     * 多账户推送
     */
    @ApiModelProperty("多账户推送")
    private List<String> manyUserAccount;

    /**
     * 消息摘要(允许推送自定义参数)
     */
    @ApiModelProperty("消息摘要(允许推送自定义参数)")
    private Map<String, String> summaryMessage;
}
