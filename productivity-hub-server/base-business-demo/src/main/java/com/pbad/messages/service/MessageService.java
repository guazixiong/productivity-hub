package com.pbad.messages.service;

import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import common.core.domain.PageResult;

/**
 * 消息推送服务接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface MessageService {

    /**
     * 发送消息
     *
     * @param sendDTO 发送请求
     * @return 发送响应
     */
    MessageSendResponseVO sendMessage(MessageSendDTO sendDTO);

    /**
     * 获取推送历史（分页，按时间倒序）
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<MessageHistoryVO> getMessageHistory(int pageNum, int pageSize);
}

