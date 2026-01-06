package com.pbad.notifications.controller;

import com.pbad.notifications.domain.dto.NotificationPublishDTO;
import com.pbad.notifications.domain.vo.NotificationVO;
import com.pbad.notifications.service.NotificationService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知消息控制器.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 内部使用：发布通知（写库 + WebSocket 推送）.
     */
    @PostMapping("/publish")
    public ApiResponse<Void> publish(@RequestBody NotificationPublishDTO dto) {
        String userId = dto.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return ApiResponse.fail("userId 不能为空");
        }
        notificationService.publish(dto);
        return ApiResponse.ok(null);
    }

    /**
     * 当前用户的通知列表（分页）.
     */
    @GetMapping
    public ApiResponse<PageResult<NotificationVO>> page(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        String userId = currentUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        PageResult<NotificationVO> result = notificationService.pageByCurrentUser(page, pageSize, userId);
        return ApiResponse.ok(result);
    }

    /**
     * 将指定通知标记为已读（仅当前用户自己的通知有效）.
     */
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") String id) {
        String userId = currentUserId();
        if (userId == null) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        notificationService.markRead(id, userId);
        return ApiResponse.ok(null);
    }

    private String currentUserId() {
        return RequestUserContext.getUserId();
    }
}


