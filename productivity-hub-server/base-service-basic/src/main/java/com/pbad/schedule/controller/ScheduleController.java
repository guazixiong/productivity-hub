package com.pbad.schedule.controller;

import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.schedule.DingTalkDigestTask;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 定时任务触发接口.
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final DailyTechDigestTask dailyTechDigestTask;
    private final DingTalkDigestTask dingTalkDigestTask;

    @PostMapping("/sendDailyDigest")
    public ApiResponse<String> triggerDigest() {
        try {
            dailyTechDigestTask.sendDailyDigest();
            return ApiResponse.ok("已触发每日热点新闻任务（Resend邮箱）");
        } catch (Exception e) {
            log.error("手动触发每日技术简报失败", e);
            return ApiResponse.fail("触发失败: " + e.getMessage());
        }
    }

    @PostMapping("/sendDingTalkDigest")
    public ApiResponse<String> sendDingTalkDigest() {
        try {
            dingTalkDigestTask.sendDingTalkDigest();
            return ApiResponse.ok("已触发每日热点新闻任务（钉钉消息）");
        } catch (Exception e) {
            log.error("手动触发每日技术简报失败", e);
            return ApiResponse.fail("触发失败: " + e.getMessage());
        }
    }
}


