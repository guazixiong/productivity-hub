package com.pbad.schedule;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.service.ConfigService;
import com.pbad.tools.domain.vo.CursorCommodityVO;
import com.pbad.tools.service.CursorShopService;
import com.pbad.thirdparty.api.MessageChannelApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cursor 邮箱自助小店定时任务.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CursorShopTask {

    @Qualifier("dingtalkChannelApi")
    private final MessageChannelApi dingtalkChannelApi;
    private final ConfigService configService;
    private final CursorShopService cursorShopService;
    private final UserMapper userMapper;
    private final UserRoleUtil userRoleUtil;

    public void execute() {
        execute(null);
    }

    /**
     * 执行 Cursor 小店库存播报
     * 
     * @param targetUserId 目标用户ID，如果为null则推送给所有开启了定时任务的用户
     */
    public void execute(String targetUserId) {
        if (!isTaskEnabled("cursorShop.enabled")) {
            log.info("Cursor 邮箱自助小店任务已被关闭，跳过执行");
            return;
        }
        log.info("开始执行 Cursor 邮箱自助小店任务");
        
        // 先获取开启了定时任务的用户
        List<UserPO> enabledUsers = getEnabledUsers("cursorShop.enabled", targetUserId);
        if (enabledUsers == null || enabledUsers.isEmpty()) {
            log.info("没有开启了定时任务的用户，跳过执行");
            return;
        }
        
        // 数据信息只获取一次
        List<CursorCommodityVO> commodities = cursorShopService.fetchCommodities();
        if (commodities.isEmpty()) {
            log.info("未获取到商品数据，跳过推送");
            return;
        }

        String markdown = buildMarkdown(commodities);
        int successCount = 0;
        int failCount = 0;

        // 分别推送给不同的用户
        for (UserPO user : enabledUsers) {
            String userId = user.getId();
            
            // 跳过超级管理员（用户名是 admin）
            if (userRoleUtil.isSuperAdmin(userId)) {
                log.debug("用户 {} 是超级管理员，跳过推送", userId);
                continue;
            }
            
            // 检查用户是否配置了钉钉 webhook
            String webhook = getConfigValueSafely("dingtalk", "dingtalk.webhook", userId);
            if (webhook == null || webhook.trim().isEmpty()) {
                log.debug("用户 {} 未配置钉钉 Webhook 地址，跳过推送", userId);
                continue;
            }

            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("msgType", "markdown");
                payload.put("content", markdown);

                Map<String, String> cfg = new HashMap<>();
                cfg.put("webhook", webhook);
                String sign = getConfigValueSafely("dingtalk", "dingtalk.sign", userId);
                if (sign != null && !sign.trim().isEmpty()) {
                    cfg.put("sign", sign);
                }
                
                dingtalkChannelApi.sendMessage(payload, cfg);
                successCount++;
                log.debug("已向用户 {} 发送 Cursor 小店库存播报", userId);
            } catch (Exception e) {
                failCount++;
                log.error("向用户 {} 发送 Cursor 小店库存播报失败: {}", userId, e.getMessage(), e);
            }
        }

        log.info("Cursor 邮箱自助小店任务执行完成，成功：{}，失败：{}", successCount, failCount);
    }

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
     * @param configKey 定时任务配置键（如：cursorShop.enabled）
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

    public LocalDateTime nextAllowedExecution(LocalDateTime from) {
        LocalDateTime candidate = alignToNextHour(from);
        while (true) {
            candidate = adjustToWorkWindow(candidate);
            if (isBlockedHour(candidate)) {
                candidate = alignToNextHour(candidate.plusHours(1));
                continue;
            }
            return candidate;
        }
    }

    private LocalDateTime alignToNextHour(LocalDateTime time) {
        LocalDateTime hour = time.withMinute(0).withSecond(0).withNano(0);
        if (time.getMinute() > 0 || time.getSecond() > 0 || time.getNano() > 0) {
            return hour.plusHours(1);
        }
        return hour;
    }

    private LocalDateTime adjustToWorkWindow(LocalDateTime time) {
        LocalDateTime t = time;
        while (true) {
            if (isWeekend(t)) {
                t = t.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                continue;
            }
            int hour = t.getHour();
            if (hour < 9) {
                t = t.withHour(9).withMinute(0).withSecond(0).withNano(0);
                continue;
            }
            if (hour > 18) {
                t = t.plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
                continue;
            }
            return t;
        }
    }

    private boolean isBlockedHour(LocalDateTime time) {
        int hour = time.getHour();
        return hour >= 11 && hour <= 15;
    }

    private boolean isWeekend(LocalDateTime time) {
        switch (time.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                return true;
            default:
                return false;
        }
    }

    private String buildMarkdown(List<CursorCommodityVO> commodities) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Cursor 邮箱自助小店库存播报**  \n");
        sb.append("更新时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("  \n\n");
        sb.append("| 商品 | 价格 | 库存 | 销量 | 库存状态 |  \n");
        sb.append("| --- | --- | --- | --- | --- |  \n");
        for (CursorCommodityVO c : commodities) {
            sb.append("| ")
                    .append(escape(c.getName()))
                    .append(" | ")
                    .append(formatPrice(c.getPrice()))
                    .append(" | ")
                    .append(valueOrDash(c.getStock()))
                    .append(" | ")
                    .append(valueOrDash(c.getOrderSold()))
                    .append(" | ")
                    .append(c.getStockStateText())
                    .append(" |  \n");
        }
        return sb.toString();
    }

    private String escape(String text) {
        if (text == null) {
            return "-";
        }
        return text.replace("|", "\\|");
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "-";
        }
        return price.stripTrailingZeros().toPlainString();
    }

    private String valueOrDash(Integer value) {
        return value == null ? "-" : String.valueOf(value);
    }
}


