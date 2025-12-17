package com.pbad.messages.service;

import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.domain.vo.MessageHistoryVO;
import com.pbad.messages.domain.vo.MessageSendResponseVO;
import common.core.domain.PageResult;

/**
 * 消息推送服务接口.
 */
public interface MessageService {

    MessageSendResponseVO sendMessage(MessageSendDTO sendDTO, String userId);

    PageResult<MessageHistoryVO> getMessageHistory(int pageNum, int pageSize, String userId);
}


