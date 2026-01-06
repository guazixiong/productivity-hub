package com.pbad.health.schedule;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.service.ConfigService;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import com.pbad.thirdparty.api.MessageChannelApi;
import org.springframework.beans.factory.annotation.Qualifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钉钉饮水提醒定时任务.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WaterReminderTask {

    private final HealthWaterTargetMapper waterTargetMapper;
    private final HealthWaterIntakeMapper waterIntakeMapper;
    @Qualifier("dingtalkChannelApi")
    private final MessageChannelApi dingtalkChannelApi;
    private final ConfigService configService;
    private final UserRoleUtil userRoleUtil;
    private final UserMapper userMapper;

    // 默认每日目标饮水量（毫升）
    private static final int DEFAULT_DAILY_TARGET_ML = 2000;

    /**
     * 每小时整点执行（8:00-11:00, 14:00-18:00）
     */
    @Scheduled(cron = "0 0 8-11,14-18 * * ?", zone = "Asia/Shanghai")
    public void sendWaterReminder() {
        sendWaterReminder(null);
    }

    /**
     * 发送饮水提醒
     * 
     * @param targetUserId 目标用户ID，如果为null则推送给所有开启了定时任务的用户
     */
    public void sendWaterReminder(String targetUserId) {
        if (!isTaskEnabled()) {
            log.info("钉钉饮水提醒任务已被关闭，跳过执行");
            return;
        }

        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        // 排除13:00（午休时间）
        if (hour == 13) {
            return;
        }

        log.info("开始执行钉钉饮水提醒任务，当前时间：{}", now);

        try {
            // 先获取开启了定时任务的用户
            List<UserPO> enabledUsers = getEnabledUsers("waterReminder.enabled", targetUserId);
            if (enabledUsers == null || enabledUsers.isEmpty()) {
                log.info("没有开启了定时任务的用户，跳过执行");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (UserPO user : enabledUsers) {
                String userId = user.getId();
                
                // 跳过超级管理员（用户名是 admin）
                if (userRoleUtil.isSuperAdmin(userId)) {
                    log.debug("用户 {} 是超级管理员，跳过提醒", userId);
                    continue;
                }
                
                // 检查用户是否有饮水目标配置
                HealthWaterTargetPO target = waterTargetMapper.selectByUserId(userId);
                if (target == null) {
                    log.debug("用户 {} 未配置饮水目标，跳过提醒", userId);
                    continue;
                }
                
                try {
                    sendReminderToUser(userId);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("向用户 {} 发送饮水提醒失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("钉钉饮水提醒任务执行完成，成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("钉钉饮水提醒任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 向指定用户发送饮水提醒
     */
    private void sendReminderToUser(String userId) {
        // 从用户专属全局配置中获取钉钉Webhook地址
        String webhookUrl = getConfigValueSafely("dingtalk", "dingtalk.webhook", userId);
        
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.debug("用户 {} 的全局配置中未配置钉钉Webhook地址，跳过提醒", userId);
            return;
        }
        
        // 读取签名配置（如果有）
        String sign = getConfigValueSafely("dingtalk", "dingtalk.sign", userId);

        // 查询用户饮水目标
        HealthWaterTargetPO target = waterTargetMapper.selectByUserId(userId);
        int targetMl = target != null && target.getDailyTargetMl() != null
                ? target.getDailyTargetMl()
                : DEFAULT_DAILY_TARGET_ML;

        // 查询今日已饮水量
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Integer consumedMl = waterIntakeMapper.sumVolumeByDate(userId, todayStart);
        consumedMl = consumedMl != null ? consumedMl : 0;

        // 计算剩余需饮水量
        int remainingMl = Math.max(0, targetMl - consumedMl);

        // 计算完成进度
        double progress = targetMl > 0 ? (double) consumedMl / targetMl * 100.0 : 0.0;

        // 构建提醒消息
        String markdown = buildReminderMarkdown(consumedMl, targetMl, remainingMl, progress);

        // 发送钉钉消息
        Map<String, Object> dingTalkPayload = new HashMap<>();
        dingTalkPayload.put("msgType", "markdown");
        dingTalkPayload.put("content", markdown);

        Map<String, String> cfg = new HashMap<>();
        cfg.put("webhook", webhookUrl);
        if (sign != null && !sign.trim().isEmpty()) {
            cfg.put("sign", sign);
        }
        
        dingtalkChannelApi.sendMessage(dingTalkPayload, cfg);

        log.debug("已向用户 {} 发送饮水提醒", userId);
    }

    /**
     * 构建提醒消息内容
     */
    private String buildReminderMarkdown(int consumedMl, int targetMl, int remainingMl, double progress) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 💧 健康提醒 - 该去喝水了\n\n");
        sb.append("---\n\n");

        sb.append("### 📊 今日饮水进度\n\n");
        sb.append("- **已饮水量**：").append(consumedMl).append(" ml\n");
        sb.append("- **目标饮水量**：").append(targetMl).append(" ml\n");
        sb.append("- **剩余需饮水量**：").append(remainingMl).append(" ml\n");
        sb.append("- **完成进度**：").append(String.format("%.1f", progress)).append("%\n\n");

        // 进度条
        int progressBarLength = 20;
        int filledLength = (int) (progress / 100.0 * progressBarLength);
        sb.append("```\n");
        for (int i = 0; i < progressBarLength; i++) {
            if (i < filledLength) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        sb.append("\n```\n\n");

        // 鼓励话语
        if (progress >= 100) {
            sb.append("🎉 **太棒了！今日饮水目标已完成！**\n");
            sb.append("继续保持良好的饮水习惯，让身体更健康！\n");
        } else if (progress >= 80) {
            sb.append("👍 **很棒！已经完成大部分目标了！**\n");
            sb.append("再喝一点水就能完成今日目标了，加油！\n");
        } else if (progress >= 50) {
            sb.append("💪 **不错！已经完成一半了！**\n");
            sb.append("记得多喝水，保持身体水分充足！\n");
        } else {
            sb.append("💧 **提醒：记得多喝水哦！**\n");
            sb.append("保持充足的水分摄入，有助于身体健康！\n");
        }

        sb.append("\n---\n");
        sb.append("⭐ 由 Productivity Hub 自动提醒\n");

        return sb.toString();
    }

    /**
     * 检查任务是否启用
     */
    private boolean isTaskEnabled() {
        return isTaskEnabled("waterReminder.enabled");
    }

    /**
     * 检查任务是否启用
     */
    private boolean isTaskEnabled(String key) {
        try {
            String value = configService.getTemplateConfigValue("schedule", key);
            return !"false".equalsIgnoreCase(value) && !"0".equals(value);
        } catch (Exception ex) {
            // 如果配置不存在或读取失败，默认视为开启
            return true;
        }
    }

    /**
     * 获取开启了定时任务的用户列表
     * 
     * @param configKey 定时任务配置键（如：waterReminder.enabled）
     * @param targetUserId 目标用户ID，如果为null则返回所有开启了定时任务的用户
     * @return 开启了定时任务的用户列表
     */
    private List<UserPO> getEnabledUsers(String configKey, String targetUserId) {
        // 先检查全局开关
        if (!isTaskEnabled(configKey)) {
            return new ArrayList<>();
        }
        
        // 如果指定了目标用户，只返回该用户（如果开启了定时任务）
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            UserPO user = userMapper.selectById(targetUserId);
            if (user != null && isUserTaskEnabled(configKey, targetUserId)) {
                List<UserPO> result = new ArrayList<>();
                result.add(user);
                return result;
            }
            return new ArrayList<>();
        }
        
        // 查询所有用户
        List<UserPO> allUsers = userMapper.selectAll();
        if (allUsers == null || allUsers.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 过滤出开启了定时任务的用户
        List<UserPO> enabledUsers = new ArrayList<>();
        for (UserPO user : allUsers) {
            String userId = user.getId();
            if (isUserTaskEnabled(configKey, userId)) {
                enabledUsers.add(user);
            }
        }
        
        return enabledUsers;
    }

    /**
     * 检查用户是否开启了定时任务
     * 优先检查用户级别的配置，如果没有则使用全局配置
     * 
     * @param configKey 定时任务配置键
     * @param userId 用户ID
     * @return 是否开启
     */
    private boolean isUserTaskEnabled(String configKey, String userId) {
        try {
            // 先尝试获取用户级别的配置
            String userValue = getConfigValueSafely("schedule", configKey, userId);
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
     * 安全地获取配置值，如果配置不存在则返回null而不是抛出异常
     */
    private String getConfigValueSafely(String module, String key, String userId) {
        try {
            return configService.getConfigValue(module, key, userId);
        } catch (Exception ex) {
            return null;
        }
    }
}

