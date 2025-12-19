package com.pbad.notifications.service;

import com.pbad.notifications.domain.dto.NotificationPublishDTO;
import com.pbad.notifications.domain.vo.NotificationVO;
import common.core.domain.PageResult;

public interface NotificationService {

    /**
     * 发布一条通知：写入数据库并通过 WebSocket 推送给用户.
     */
    void publish(NotificationPublishDTO dto);

    /**
     * 查询当前用户的通知列表（分页）.
     */
    PageResult<NotificationVO> pageByCurrentUser(int pageNum, int pageSize, String userId);

    /**
     * 标记指定通知为已读（仅限当前用户）.
     */
    void markRead(String id, String userId);
}


