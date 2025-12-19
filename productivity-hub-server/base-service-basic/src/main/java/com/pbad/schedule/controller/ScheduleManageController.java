package com.pbad.schedule.controller;

import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.service.ConfigService;
import com.pbad.schedule.DailyTechDigestTask;
import com.pbad.schedule.DingTalkDigestTask;
import com.pbad.schedule.CursorShopTask;
import com.pbad.schedule.domain.dto.ScheduleToggleUpdateDTO;
import com.pbad.schedule.domain.vo.ScheduleTaskVO;
import com.pbad.tools.schedule.ToolStatSyncTask;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final String MODULE = "schedule";

    private enum TaskDef {
        DAILY_DIGEST_RESEND("dailyDigestResend", "每日热点速览（Resend 邮箱）",
                "每天 7/12/18 点通过 Resend 邮箱推送热点新闻", "0 0 7,12,18 * * ?", "dailyTechDigest.enabled"),
        DAILY_DIGEST_DINGTALK("dailyDigestDingTalk", "每日热点速览（钉钉）",
                "每天 7/12/18 点通过钉钉推送热点新闻和天气、每日一言", "0 0 7,12,18 * * ?", "dingTalkDigest.enabled"),
        CURSOR_SHOP("cursorShopTask", "Cursor 小店库存播报",
                "工作日 9:00-18:00 内按规则定期推送库存播报到钉钉", "自定义触发器（小时级窗口）", "cursorShop.enabled"),
        TOOL_STAT_SYNC("toolStatSync", "工具统计数据落库",
                "每小时同步工具使用统计到数据库", "0 0 * * * ?", "toolStatSync.enabled");

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
     */
    @GetMapping("/tasks")
    public ApiResponse<List<ScheduleTaskVO>> listTasks() {
        List<ScheduleTaskVO> list = new ArrayList<>();
        for (TaskDef def : TaskDef.values()) {
            ScheduleTaskVO vo = new ScheduleTaskVO();
            vo.setId(def.id);
            vo.setName(def.name);
            vo.setDescription(def.description);
            vo.setCron(def.cron);
            vo.setEnabled(isTaskEnabled(def.configKey));
            list.add(vo);
        }
        return ApiResponse.ok(list);
    }

    /**
     * 更新任务启用状态.
     */
    @PostMapping("/toggle")
    public ApiResponse<String> toggleTask(@RequestBody ScheduleToggleUpdateDTO dto) {
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
     * 一键启用所有任务.
     */
    @PostMapping("/enableAll")
    public ApiResponse<String> enableAllTasks() {
        return batchToggleAllTasks(true);
    }

    /**
     * 一键关闭所有任务.
     */
    @PostMapping("/disableAll")
    public ApiResponse<String> disableAllTasks() {
        return batchToggleAllTasks(false);
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
     * 手动触发指定任务执行.
     */
    @PostMapping("/trigger/{id}")
    public ApiResponse<String> triggerTask(@PathVariable("id") String id) {
        TaskDef def = findTask(id);
        if (def == null) {
            return ApiResponse.fail("未知的任务 ID");
        }
        try {
            switch (def) {
                case DAILY_DIGEST_RESEND:
                    dailyTechDigestTask.sendDailyDigest();
                    break;
                case DAILY_DIGEST_DINGTALK:
                    dingTalkDigestTask.sendDingTalkDigest();
                    break;
                case CURSOR_SHOP:
                    cursorShopTask.execute();
                    break;
                case TOOL_STAT_SYNC:
                    toolStatSyncTask.syncToolStatsToDatabase();
                    break;
                default:
                    return ApiResponse.fail("暂不支持的任务类型");
            }
            // 同样返回文案到 data，便于前端直接使用
            return ApiResponse.ok("任务已触发执行", "任务已触发执行");
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

    private boolean isTaskEnabled(String configKey) {
        try {
            String value = configService.getTemplateConfigValue(MODULE, configKey);
            return !"false".equalsIgnoreCase(value) && !"0".equals(value);
        } catch (Exception ex) {
            // 未配置或读取失败时默认开启，避免影响任务执行
            return true;
        }
    }
}


