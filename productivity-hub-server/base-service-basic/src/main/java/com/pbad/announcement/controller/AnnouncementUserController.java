package com.pbad.announcement.controller;

import com.pbad.announcement.domain.vo.AnnouncementVO;
import com.pbad.announcement.service.AnnouncementService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告用户控制器（普通用户使用）.
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementUserController {

    private final AnnouncementService announcementService;

    /**
     * 获取当前用户的未读公告列表.
     */
    @GetMapping("/unread")
    public ApiResponse<List<AnnouncementVO>> getUnread() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        List<AnnouncementVO> list = announcementService.getUnreadByUser(userId);
        return ApiResponse.ok(list);
    }

    /**
     * 标记公告为已读.
     */
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        announcementService.markRead(id, userId);
        return ApiResponse.ok("标记成功", null);
    }

    /**
     * 获取公告详情（用户查看）.
     */
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementVO> getById(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        AnnouncementVO vo = announcementService.getById(id);
        if (vo == null) {
            return ApiResponse.fail(404, "公告不存在");
        }
        // 检查公告是否已发布且未过期
        if (!"PUBLISHED".equals(vo.getStatus())) {
            return ApiResponse.fail(403, "公告未发布");
        }
        return ApiResponse.ok(vo);
    }
}

