package com.pbad.schedule;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.service.ConfigService;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import com.pbad.thirdparty.api.DailyQuoteApi;
import com.pbad.thirdparty.api.HotDataApi;
import com.pbad.thirdparty.api.ShortLinkApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日热点新闻推送任务（Resend 邮箱渠道）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTechDigestTask {

    private static final int LIMIT = 5;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MessageService messageService;
    private final HotDataApi hotDataApi;
    private final DailyQuoteApi dailyQuoteApi;
    private final ConfigService configService;
    private final ShortLinkApi shortLinkApi;
    private final UserMapper userMapper;
    private final UserRoleUtil userRoleUtil;

    @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Shanghai")
    public void sendDailyDigest() {
        sendDailyDigest(null);
    }

    /**
     * 发送每日热点新闻
     * 
     * @param targetUserId 目标用户ID，如果为null则推送给所有开启了定时任务的用户
     */
    public void sendDailyDigest(String targetUserId) {
        if (!isTaskEnabled("dailyTechDigest.enabled")) {
            log.info("每日热点新闻任务（Resend 邮箱）已被关闭，跳过执行");
            return;
        }
        log.info("开始执行每日热点新闻任务（Resend 邮箱）");
        try {
            // 先获取开启了定时任务的用户
            List<UserPO> enabledUsers = getEnabledUsers("dailyTechDigest.enabled", targetUserId);
            if (enabledUsers == null || enabledUsers.isEmpty()) {
                log.info("没有开启了定时任务的用户，跳过执行");
                return;
            }

            // 数据信息只获取一次
            DigestData digestData = buildDigestData();
            String subject = "每日热点速览 | " + LocalDate.now().format(DATE_FMT);
            String html = renderHtml(digestData);

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
                
                // 检查用户是否配置了 resend 邮箱
                String toEmail = getConfigValueSafely("resend", "resend.toEmail", userId);
                if (toEmail == null || toEmail.trim().isEmpty()) {
                    log.debug("用户 {} 未配置 Resend 邮箱地址，跳过推送", userId);
                    continue;
                }

                try {
                    Map<String, Object> resendPayload = new HashMap<>();
                    resendPayload.put("title", subject);
                    resendPayload.put("html", html);
                    MessageSendDTO resendDto = new MessageSendDTO();
                    resendDto.setChannel("resend");
                    resendDto.setData(resendPayload);
                    messageService.sendMessage(resendDto, userId);
                    successCount++;
                    log.debug("已向用户 {} 发送每日热点推送", userId);
                } catch (Exception e) {
                    failCount++;
                    log.error("向用户 {} 发送每日热点推送失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("每日热点新闻推送任务执行完成，成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("每日热点新闻推送失败: {}", e.getMessage(), e);
        }
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
     * @param configKey 定时任务配置键（如：dailyTechDigest.enabled）
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

    private DigestData buildDigestData() {
        List<HotDataApi.HotSectionVO> sections = hotDataApi.getHotSections(LIMIT);
        List<Section> sectionList = new ArrayList<>();
        for (HotDataApi.HotSectionVO sectionVO : sections) {
            List<HotItem> items = new ArrayList<>();
            for (HotDataApi.HotItemVO itemVO : sectionVO.getItems()) {
                items.add(new HotItem(itemVO.getTitle(), itemVO.getLink(), itemVO.getHeat(), itemVO.getDesc()));
            }
            sectionList.add(new Section(sectionVO.getName(), items));
        }
        return new DigestData(sectionList);
    }

    private String renderHtml(DigestData digest) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',Arial,sans-serif;background:#f8fafc;padding:24px;color:#0f172a;\">");
        sb.append("<style>")
                .append(".card{background:#fff;border-radius:14px;padding:16px;margin-bottom:12px;box-shadow:0 8px 24px rgba(15,23,42,0.06);} ")
                .append(".title{font-size:18px;font-weight:700;margin:0 0 6px;} ")
                .append(".subtitle{color:#64748b;font-size:13px;margin-bottom:12px;} ")
                .append(".pill{display:inline-block;background:#e0f2fe;color:#0ea5e9;padding:3px 10px;border-radius:999px;font-size:12px;margin-right:8px;} ")
                .append(".section-title{font-size:16px;font-weight:700;margin:18px 0 10px;display:flex;align-items:center;gap:8px;} ")
                .append("a{color:#2563eb;text-decoration:none;} a:hover{text-decoration:underline;} ")
                .append(".heat{color:#f97316;font-size:12px;margin-left:6px;} ")
                .append(".desc{color:#475569;font-size:13px;margin-top:4px;line-height:1.4;} ")
                .append("</style>");

        sb.append("<div class=\"card\" style=\"border:1px solid #e2e8f0;\">")
                .append("<div class=\"title\">每日热点速览</div>")
                .append("<div class=\"subtitle\">")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append(" · 早 7 点自动送达")
                .append("</div>");
        for (Section section : digest.getSections()) {
            sb.append("<span class=\"pill\">").append(section.getName()).append("</span>");
        }
        sb.append("</div>");

        for (Section section : digest.getSections()) {
            sb.append("<div class=\"section\">")
                    .append("<div class=\"section-title\">").append(escape(section.getName())).append("</div>");
            for (HotItem item : section.getItems()) {
                // 邮箱推送使用原始URL，不使用短链
                String linkUrl = item.getLink();
                
                sb.append("<div class=\"card\">")
                        .append("<div class=\"title\"><a href=\"").append(linkUrl).append("\" target=\"_blank\">")
                        .append(escape(item.getTitle()))
                        .append("</a>");
                if (!isBlank(item.getHeat())) {
                    sb.append("<span class=\"heat\">").append(escape(item.getHeat())).append("</span>");
                }
                sb.append("</div>");
                if (!isBlank(item.getDesc())) {
                    sb.append("<div class=\"desc\">").append(escape(item.getDesc())).append("</div>");
                }
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        sb.append("<div class=\"subtitle\" style=\"margin-top:16px;\">由 Productivity Hub · Resend 自动送达</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    @Data
    @AllArgsConstructor
    private static class HotItem {
        private String title;
        private String link;
        private String heat;
        private String desc;
    }

    @Data
    @AllArgsConstructor
    private static class Section {
        private String name;
        private List<HotItem> items;
    }

    @Data
    @AllArgsConstructor
    private static class DigestData {
        private List<Section> sections;
    }
}


