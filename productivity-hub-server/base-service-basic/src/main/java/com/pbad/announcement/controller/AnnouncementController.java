package com.pbad.announcement.controller;

import com.pbad.announcement.domain.dto.AnnouncementCreateDTO;
import com.pbad.announcement.domain.dto.AnnouncementUpdateDTO;
import com.pbad.announcement.domain.vo.AnnouncementVO;
import com.pbad.announcement.domain.vo.AnnouncementStatsVO;
import com.pbad.announcement.service.AnnouncementService;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 公告管理控制器（管理员使用）.
 */
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserMapper userMapper;

    /**
     * 创建公告.
     */
    @PostMapping
    public ApiResponse<AnnouncementVO> create(@RequestBody AnnouncementCreateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以创建公告");
        }
        AnnouncementVO vo = announcementService.create(dto, userId);
        return ApiResponse.ok("创建成功", vo);
    }

    /**
     * 更新公告.
     */
    @PutMapping("/{id}")
    public ApiResponse<AnnouncementVO> update(@PathVariable("id") String id,
                                              @RequestBody AnnouncementUpdateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以更新公告");
        }
        AnnouncementVO vo = announcementService.update(id, dto);
        return ApiResponse.ok("更新成功", vo);
    }

    /**
     * 删除公告.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以删除公告");
        }
        announcementService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    /**
     * 发布公告.
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<AnnouncementVO> publish(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以发布公告");
        }
        AnnouncementVO vo = announcementService.publish(id);
        return ApiResponse.ok("发布成功", vo);
    }

    /**
     * 撤回公告.
     */
    @PostMapping("/{id}/withdraw")
    public ApiResponse<AnnouncementVO> withdraw(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以撤回公告");
        }
        AnnouncementVO vo = announcementService.withdraw(id);
        return ApiResponse.ok("撤回成功", vo);
    }

    /**
     * 获取公告详情.
     */
    @GetMapping("/{id}")
    public ApiResponse<AnnouncementVO> getById(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以查看公告详情");
        }
        AnnouncementVO vo = announcementService.getById(id);
        return ApiResponse.ok(vo);
    }

    /**
     * 分页查询公告列表.
     */
    @GetMapping
    public ApiResponse<PageResult<AnnouncementVO>> page(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "status", required = false) String status) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以查看公告列表");
        }
        PageResult<AnnouncementVO> result = announcementService.page(page, pageSize, status);
        return ApiResponse.ok(result);
    }

    /**
     * 获取公告阅读统计.
     */
    @GetMapping("/{id}/stats")
    public ApiResponse<AnnouncementStatsVO> getStats(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以查看公告统计");
        }
        AnnouncementStatsVO stats = announcementService.getStats(id);
        return ApiResponse.ok(stats);
    }

    private boolean isAdmin(String userId) {
        UserPO current = userMapper.selectById(userId);
        if (current == null || current.getRoles() == null) {
            return false;
        }
        return current.getRoles().contains("\"admin\"");
    }
}

