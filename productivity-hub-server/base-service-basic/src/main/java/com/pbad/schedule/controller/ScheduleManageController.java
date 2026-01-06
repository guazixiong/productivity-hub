package com.pbad.schedule.controller;

import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.service.ConfigService;
import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.schedule.DingTalkDigestTask;
import com.pbad.schedule.CursorShopTask;
import com.pbad.schedule.domain.dto.ScheduleToggleUpdateDTO;
import com.pbad.schedule.domain.vo.ScheduleTaskVO;
import com.pbad.tools.schedule.ToolStatSyncTask;
import com.pbad.health.schedule.WaterReminderTask;
import com.pbad.health.schedule.HealthDailyReportTask;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务管理接口（启停 & 手动触发）.
 */
@Slf4j
@RestController
@RequestMapping("/api/schedule/manage")
@RequiredArgsConstructor
public class ScheduleManageController {

    private final ConfigService configService;
    private final DailyTechDigestTask dailyTechDigestTask;
    private final DingTalkDigestTask dingTalkDigestTask;
    private final CursorShopTask cursorShopTask;
    private final ToolStatSyncTask toolStatSyncTask;
    private final WaterReminderTask waterReminderTask;
    private final HealthDailyReportTask healthDailyReportTask;
    private final UserRoleUtil userRoleUtil;

    private static final String MODULE = "schedule";

    private enum TaskDef {
        DAILY_DIGEST_RESEND("dailyDigestResend", "每日热点速览（Resend 邮箱）",
                "每天 7/12/18 点通过 Resend 邮箱推送热点新闻", "0 0 7,12,18 * * ?", "dailyTechDigest.enabled"),
        DAILY_DIGEST_DINGTALK("dailyDigestDingTalk", "每日热点速览（钉钉）",
                "每天 7/12/18 点通过钉钉推送热点新闻和天气、每日一言", "0 0 7,12,18 * * ?", "dingTalkDigest.enabled"),
        CURSOR_SHOP("cursorShopTask", "Cursor 小店库存播报",
                "工作日 9:00-18:00 内按规则定期推送库存播报到钉钉", "自定义触发器（小时级窗口）", "cursorShop.enabled"),
//        TOOL_STAT_SYNC("toolStatSync", "工具统计数据落库",
//                "每小时同步工具使用统计到数据库", "0 0 * * * ?", "toolStatSync.enabled"),
        WATER_REMINDER("waterReminder", "钉钉饮水提醒",
                "每天 7:00-12:00、14:00-21:00 每隔1小时通过钉钉推送饮水提醒", "0 0 7-12,14-21 * * ?", "waterReminder.enabled"),
        HEALTH_DAILY_REPORT("healthDailyReport", "每日健康统计邮件推送",
                "每天 7:00 通过Resend邮件推送前一日健康统计数据", "0 0 7 * * ?", "healthDailyReport.enabled");

        private final String id;
        private final String name;
        private final String description;
        private final String cron;
        private final String configKey;

        TaskDef(String id, String name, String description, String cron, String configKey) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.cron = cron;
            this.configKey = configKey;
        }
    }

    /**
     * 列出所有可管理的定时任务及其当前开关状态.
     * 所有用户都可以查看，显示的是用户自己的定时任务开关状态.
     */
    @GetMapping("/tasks")
    public ApiResponse<List<ScheduleTaskVO>> listTasks() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        
        List<ScheduleTaskVO> list = new ArrayList<>();
        for (TaskDef def : TaskDef.values()) {
            ScheduleTaskVO vo = new ScheduleTaskVO();
            vo.setId(def.id);
            vo.setName(def.name);
            vo.setDescription(def.description);
            vo.setCron(def.cron);
            // 显示用户自己的定时任务开关状态
            vo.setEnabled(isUserTaskEnabled(def.configKey, userId));
            list.add(vo);
        }
        return ApiResponse.ok(list);
    }

    /**
     * 更新任务启用状态（全局配置）.
     * 仅管理员可以操作.
     */
    @PostMapping("/toggle")
    public ApiResponse<String> toggleTask(@RequestBody ScheduleToggleUpdateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!userRoleUtil.isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以管理定时任务");
        }
        
        if (dto == null || dto.getId() == null || dto.getEnabled() == null) {
            return ApiResponse.fail("参数不完整");
        }
        TaskDef def = findTask(dto.getId());
        if (def == null) {
            return ApiResponse.fail("未知的任务 ID");
        }
        try {
            ConfigCreateOrUpdateDTO cfg = new ConfigCreateOrUpdateDTO();
            cfg.setModule(MODULE);
            cfg.setKey(def.configKey);
            cfg.setValue(Boolean.TRUE.equals(dto.getEnabled()) ? "true" : "false");
            cfg.setDescription("定时任务开关：" + def.name);
            // 这里使用模板用户 ID（system）来修改模板配置
            configService.createOrUpdateConfig(cfg, "system", "system");
            // message 用于提示，data 返回同样的文案，方便前端直接展示
            return ApiResponse.ok("已更新任务开关", "已更新任务开关");
        } catch (Exception e) {
            log.error("更新任务 {} 开关失败", def.id, e);
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 用户开启/关闭自己的定时任务.
     * 所有用户都可以操作自己的定时任务开关.
     */
    @PostMapping("/toggleMyTask")
    public ApiResponse<String> toggleMyTask(@RequestBody ScheduleToggleUpdateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        
        if (dto == null || dto.getId() == null || dto.getEnabled() == null) {
            return ApiResponse.fail("参数不完整");
        }
        TaskDef def = findTask(dto.getId());
        if (def == null) {
            return ApiResponse.fail("未知的任务 ID");
        }
        try {
            ConfigCreateOrUpdateDTO cfg = new ConfigCreateOrUpdateDTO();
            cfg.setModule(MODULE);
            cfg.setKey(def.configKey);
            cfg.setValue(Boolean.TRUE.equals(dto.getEnabled()) ? "true" : "false");
            cfg.setDescription("定时任务开关：" + def.name);
            // 使用当前用户 ID 来修改用户自己的配置
            configService.createOrUpdateConfig(cfg, userId, userId);
            String action = Boolean.TRUE.equals(dto.getEnabled()) ? "开启" : "关闭";
            String message = "已" + action + "您的定时任务：" + def.name;
            return ApiResponse.ok(message, message);
        } catch (Exception e) {
            log.error("更新用户 {} 的任务 {} 开关失败", userId, def.id, e);
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }

    /**
     * 一键启用所有任务.
     * 仅管理员可以操作.
     */
    @PostMapping("/enableAll")
    public ApiResponse<String> enableAllTasks() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!userRoleUtil.isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以管理定时任务");
        }
        return batchToggleAllTasks(true);
    }

    /**
     * 一键关闭所有任务.
     * 仅管理员可以操作.
     */
    @PostMapping("/disableAll")
    public ApiResponse<String> disableAllTasks() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        if (!userRoleUtil.isAdmin(userId)) {
            return ApiResponse.fail(403, "仅管理员可以管理定时任务");
        }
        return batchToggleAllTasks(false);
    }

    /**
     * 用户一键启用自己的所有定时任务.
     * 所有用户都可以操作自己的定时任务开关.
     */
    @PostMapping("/enableAllMyTasks")
    public ApiResponse<String> enableAllMyTasks() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        return batchToggleMyTasks(userId, true);
    }

    /**
     * 用户一键关闭自己的所有定时任务.
     * 所有用户都可以操作自己的定时任务开关.
     */
    @PostMapping("/disableAllMyTasks")
    public ApiResponse<String> disableAllMyTasks() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        return batchToggleMyTasks(userId, false);
    }

    private ApiResponse<String> batchToggleAllTasks(boolean enabled) {
        String targetValue = enabled ? "true" : "false";
        String actionText = enabled ? "启用" : "关闭";
        try {
            for (TaskDef def : TaskDef.values()) {
                ConfigCreateOrUpdateDTO cfg = new ConfigCreateOrUpdateDTO();
                cfg.setModule(MODULE);
                cfg.setKey(def.configKey);
                cfg.setValue(targetValue);
                cfg.setDescription("定时任务开关：" + def.name);
                // 使用模板用户 ID（system）批量更新所有任务的模板配置
                configService.createOrUpdateConfig(cfg, "system", "system");
            }
            String msg = "已" + actionText + "所有定时任务";
            return ApiResponse.ok(msg, msg);
        } catch (Exception e) {
            log.error("批量{}所有任务开关失败", actionText, e);
            return ApiResponse.fail("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新用户自己的所有定时任务开关.
     */
    private ApiResponse<String> batchToggleMyTasks(String userId, boolean enabled) {
        String targetValue = enabled ? "true" : "false";
        String actionText = enabled ? "启用" : "关闭";
        try {
            for (TaskDef def : TaskDef.values()) {
                ConfigCreateOrUpdateDTO cfg = new ConfigCreateOrUpdateDTO();
                cfg.setModule(MODULE);
                cfg.setKey(def.configKey);
                cfg.setValue(targetValue);
                cfg.setDescription("定时任务开关：" + def.name);
                // 使用当前用户 ID 来修改用户自己的配置
                configService.createOrUpdateConfig(cfg, userId, userId);
            }
            String msg = "已" + actionText + "您的所有定时任务";
            return ApiResponse.ok(msg, msg);
        } catch (Exception e) {
            log.error("批量{}用户 {} 的所有任务开关失败", actionText, userId, e);
            return ApiResponse.fail("批量更新失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发指定任务执行.
     * 超级管理员可以触发所有用户的定时任务，其他用户只能触发自己的定时任务.
     */
    @PostMapping("/trigger/{id}")
    public ApiResponse<String> triggerTask(@PathVariable("id") String id) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        
        TaskDef def = findTask(id);
        if (def == null) {
            return ApiResponse.fail("未知的任务 ID");
        }
        
        // 判断是否是超级管理员（用户名必须是 admin）
        boolean isSuperAdmin = userRoleUtil.isSuperAdmin(userId);
        // 如果不是超级管理员，只能触发自己的定时任务
        String targetUserId = isSuperAdmin ? null : userId;
        
        try {
            switch (def) {
                case DAILY_DIGEST_RESEND:
                    dailyTechDigestTask.sendDailyDigest(targetUserId);
                    break;
                case DAILY_DIGEST_DINGTALK:
                    dingTalkDigestTask.sendDingTalkDigest(targetUserId);
                    break;
                case CURSOR_SHOP:
                    cursorShopTask.execute(targetUserId);
                    break;
//                case TOOL_STAT_SYNC:
//                    toolStatSyncTask.syncToolStatsToDatabase();
//                    break;
                case WATER_REMINDER:
                    waterReminderTask.sendWaterReminder(targetUserId);
                    break;
                case HEALTH_DAILY_REPORT:
                    healthDailyReportTask.sendWeeklyHealthReport(targetUserId);
                    break;
                default:
                    return ApiResponse.fail("暂不支持的任务类型");
            }
            // 同样返回文案到 data，便于前端直接使用
            String message = isSuperAdmin ? "任务已触发执行" : "已触发您自己的定时任务";
            return ApiResponse.ok(message, message);
        } catch (Exception e) {
            log.error("手动触发任务 {} 失败", id, e);
            return ApiResponse.fail("触发失败: " + e.getMessage());
        }
    }

    private TaskDef findTask(String id) {
        for (TaskDef def : TaskDef.values()) {
            if (def.id.equals(id)) {
                return def;
            }
        }
        return null;
    }

    /**
     * 检查全局定时任务是否启用（模板配置）.
     */
    private boolean isTaskEnabled(String configKey) {
        try {
            String value = configService.getTemplateConfigValue(MODULE, configKey);
            return !"false".equalsIgnoreCase(value) && !"0".equals(value);
        } catch (Exception ex) {
            // 未配置或读取失败时默认开启，避免影响任务执行
            return true;
        }
    }

    /**
     * 检查用户自己的定时任务是否启用.
     * 优先检查用户级别的配置，如果没有则使用全局配置.
     *
     * @param configKey 定时任务配置键
     * @param userId 用户ID
     * @return 是否开启
     */
    private boolean isUserTaskEnabled(String configKey, String userId) {
        try {
            // 先尝试获取用户级别的配置
            String userValue = getConfigValueSafely(MODULE, configKey, userId);
            if (userValue != null) {
                return !"false".equalsIgnoreCase(userValue) && !"0".equals(userValue);
            }
            // 如果用户没有配置，则使用全局配置
            return isTaskEnabled(configKey);
        } catch (Exception ex) {
            // 如果读取失败，默认使用全局配置
            return isTaskEnabled(configKey);
        }
    }

    /**
     * 安全地获取配置值，如果配置不存在则返回null而不是抛出异常.
     */
    private String getConfigValueSafely(String module, String key, String userId) {
        try {
            return configService.getConfigValue(module, key, userId);
        } catch (Exception ex) {
            return null;
        }
    }
}


