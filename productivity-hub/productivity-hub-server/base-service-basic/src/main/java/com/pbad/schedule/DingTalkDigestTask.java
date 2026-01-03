package com.pbad.schedule;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.service.ConfigService;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import com.pbad.thirdparty.api.DailyQuoteApi;
import com.pbad.thirdparty.api.HotDataApi;
import com.pbad.thirdparty.api.WeatherApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每日热点新闻推送任务（钉钉消息渠道）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkDigestTask {

    private static final int LIMIT = 10;

    private final MessageService messageService;
    private final HotDataApi hotDataApi;
    private final WeatherApi weatherApi;
    private final DailyQuoteApi dailyQuoteApi;
    private final ConfigService configService;
    private final UserMapper userMapper;
    private final UserRoleUtil userRoleUtil;

    @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Shanghai")
    public void sendDingTalkDigest() {
        sendDingTalkDigest(null);
    }

    /**
     * 发送每日热点新闻（钉钉消息）
     * 
     * @param targetUserId 目标用户ID，如果为null则推送给所有开启了定时任务的用户
     */
    public void sendDingTalkDigest(String targetUserId) {
        if (!isTaskEnabled("dingTalkDigest.enabled")) {
            log.info("每日热点新闻任务（钉钉消息）已被关闭，跳过执行");
            return;
        }
        log.info("开始执行每日热点新闻任务（钉钉消息）");
        try {
            // 先获取开启了定时任务的用户
            List<UserPO> enabledUsers = getEnabledUsers("dingTalkDigest.enabled", targetUserId);
            if (enabledUsers == null || enabledUsers.isEmpty()) {
                log.info("没有开启了定时任务的用户，跳过执行");
                return;
            }

            // 数据信息只获取一次
            DigestData dingTalkData = buildDigestDataForDingTalk();
            WeatherApi.WeatherInfo weatherInfo = weatherApi.getWeatherInfoByCoordinates(34.7466, 113.6254, "郑州");
            DailyQuoteApi.DailyQuote dailyQuote = dailyQuoteApi.getDailyQuote();
            String markdown = renderDingTalkMarkdown(dingTalkData, weatherInfo, dailyQuote);

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
                    Map<String, Object> dingTalkPayload = new HashMap<>();
                    dingTalkPayload.put("msgType", "markdown");
                    dingTalkPayload.put("content", markdown);
                    MessageSendDTO dingTalkDto = new MessageSendDTO();
                    dingTalkDto.setChannel("dingtalk");
                    dingTalkDto.setData(dingTalkPayload);
                    messageService.sendMessage(dingTalkDto, userId);
                    successCount++;
                    log.debug("已向用户 {} 发送每日热点推送", userId);
                } catch (Exception e) {
                    failCount++;
                    log.error("向用户 {} 发送每日热点推送失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("每日热点新闻推送任务执行完成，成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("钉钉消息推送失败: {}", e.getMessage(), e);
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
     * @param configKey 定时任务配置键（如：dingTalkDigest.enabled）
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

    private DigestData buildDigestDataForDingTalk() {
        HotDataApi.HotSectionVO sectionVO = hotDataApi.getHotSectionByName("综合热榜", LIMIT);
        List<HotItem> items = new ArrayList<>();
        for (HotDataApi.HotItemVO itemVO : sectionVO.getItems()) {
            items.add(new HotItem(itemVO.getTitle(), itemVO.getLink(), itemVO.getHeat(), itemVO.getDesc()));
        }
        List<Section> sections = new ArrayList<>();
        sections.add(new Section("综合热榜", items));
        return new DigestData(sections);
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private String renderDingTalkMarkdown(DigestData digest, WeatherApi.WeatherInfo weatherInfo, DailyQuoteApi.DailyQuote dailyQuote) {
        StringBuilder sb = new StringBuilder();

        LocalDateTime now = LocalDateTime.now();

        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        String weekDay = getWeekDayShort(now.getDayOfWeek());
        sb.append("## 📆 ").append(month).append("月").append(day).append("日 ").append(weekDay).append("\n\n");

        sb.append("---\n\n");

        sb.append("### 🌤️ 郑州天气\n\n");
        if (weatherInfo != null) {
            sb.append("**").append(weatherInfo.getWeather()).append("**");
            if (!isBlank(weatherInfo.getTemp())) {
                sb.append(" ").append(weatherInfo.getTemp()).append("°C");
            }
            if (!isBlank(weatherInfo.getWind())) {
                sb.append(" | ").append(weatherInfo.getWind());
            }
            if (!isBlank(weatherInfo.getHumidity())) {
                sb.append(" | 湿度 ").append(weatherInfo.getHumidity());
            }
            sb.append("\n\n");
        } else {
            sb.append("天气信息获取中...\n\n");
        }

        sb.append("### 💭 每日一言\n\n");
        if (dailyQuote != null && !isBlank(dailyQuote.getQuote())) {
            sb.append("> ").append(dailyQuote.getQuote()).append("\n\n");
            if (!isBlank(dailyQuote.getFrom())) {
                sb.append("—— ").append(dailyQuote.getFrom()).append("\n\n");
            }
        } else {
            sb.append("每日一言获取中...\n\n");
        }

        sb.append("---\n\n");

        for (Section section : digest.getSections()) {
            String icon = getSectionIcon(section.getName());
            sb.append("### ").append(icon).append(" ").append(section.getName()).append("\n\n");

            List<HotItem> items = section.getItems();
            if (items == null || items.isEmpty()) {
                sb.append("暂无数据\n\n");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    HotItem item = items.get(i);
                    sb.append("**").append(i + 1).append(". ").append(item.getTitle()).append("**\n");

                    boolean hasInfo = false;
                    if (!isBlank(item.getLink())) {
                        // 使用原始URL
                        String linkUrl = item.getLink();
                        sb.append("[🔗 查看详情](").append(linkUrl).append(")");
                        hasInfo = true;
                    }
                    if (!isBlank(item.getHeat())) {
                        if (hasInfo) {
                            sb.append(" · ");
                        }
                        sb.append("🔥 ").append(item.getHeat());
                        hasInfo = true;
                    }
                    if (hasInfo) {
                        sb.append("\n");
                    }

                    if (!isBlank(item.getDesc()) && item.getDesc().length() <= 80) {
                        sb.append("_").append(item.getDesc()).append("_\n");
                    }

                    if (i < items.size() - 1) {
                        sb.append("\n");
                    }
                }
            }
            sb.append("\n");
        }

        sb.append("---\n\n");
        sb.append("⭐ 由 小胖 自动推送\n");

        return sb.toString();
    }

    private String getWeekDayShort(DayOfWeek dayOfWeek) {
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weekDays[dayOfWeek.getValue() - 1];
    }

    private String getSectionIcon(String sectionName) {
        if (sectionName.contains("综合")) {
            return "📡";
        } else if (sectionName.contains("知乎")) {
            return "💡";
        } else if (sectionName.contains("微博")) {
            return "🔥";
        } else if (sectionName.contains("虎扑")) {
            return "🏀";
        } else if (sectionName.contains("小红书")) {
            return "📕";
        } else if (sectionName.contains("哔哩哔哩") || sectionName.contains("B站")) {
            return "📺";
        } else if (sectionName.contains("抖音")) {
            return "🎵";
        } else if (sectionName.contains("贴吧")) {
            return "💬";
        }
        return "📰";
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


