package com.pbad.announcement.service;

import com.pbad.announcement.domain.dto.AnnouncementCreateDTO;
import com.pbad.announcement.domain.dto.AnnouncementUpdateDTO;
import com.pbad.announcement.domain.vo.AnnouncementVO;
import com.pbad.announcement.domain.vo.AnnouncementStatsVO;
import common.core.domain.PageResult;

import java.util.List;

public interface AnnouncementService {

    /**
     * 创建公告.
     */
    AnnouncementVO create(AnnouncementCreateDTO dto, String creatorId);

    /**
     * 更新公告.
     */
    AnnouncementVO update(String id, AnnouncementUpdateDTO dto);

    /**
     * 删除公告.
     */
    void delete(String id);

    /**
     * 发布公告（立即发布或定时发布）.
     */
    AnnouncementVO publish(String id);

    /**
     * 撤回公告.
     */
    AnnouncementVO withdraw(String id);

    /**
     * 获取公告详情.
     */
    AnnouncementVO getById(String id);

    /**
     * 分页查询公告列表（管理员使用）.
     */
    PageResult<AnnouncementVO> page(int pageNum, int pageSize, String status);

    /**
     * 获取当前用户的未读公告列表.
     */
    List<AnnouncementVO> getUnreadByUser(String userId);

    /**
     * 标记公告为已读.
     */
    void markRead(String announcementId, String userId);

    /**
     * 获取公告阅读统计.
     */
    AnnouncementStatsVO getStats(String announcementId);
}

