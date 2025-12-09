package com.pbad.schedule;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import com.pbad.util.ThirdPartyApiUtil;
import com.pbad.util.ThirdPartyApiUtil.DailyQuote;
import com.pbad.util.ThirdPartyApiUtil.WeatherInfo;
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
import java.util.stream.Collectors;

/**
 * 每日热点新闻推送任务（钉钉消息渠道）。
 *
 * <p>内容：Rebang.Today 综合热榜、郑州天气、每日一言。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkDigestTask {

    private static final String REBANG_TOP_URL = "https://api.rebang.today/v1/items?tab=top&sub_tab=lasthour&page=1&version=1";

    private static final int LIMIT = 10;

    private final MessageService messageService;

    /**
     * 每天 7:00、12:00、18:00 发送钉钉消息（综合热榜、天气、每日一言）.
     */
    @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Shanghai")
    public void sendDingTalkDigest() {
        log.info("开始执行每日热点新闻任务（钉钉消息）");
        try {
            DigestData dingTalkData = buildDigestDataForDingTalk();
            // 使用郑州经纬度获取天气信息（Open-Meteo API）
            WeatherInfo weatherInfo = ThirdPartyApiUtil.getZhengzhouWeatherInfo();
            DailyQuote dailyQuote = ThirdPartyApiUtil.getDailyQuote();
            String markdown = renderDingTalkMarkdown(dingTalkData, weatherInfo, dailyQuote);
            Map<String, Object> dingTalkPayload = new HashMap<>();
            dingTalkPayload.put("msgType", "markdown");
            dingTalkPayload.put("content", markdown);
            MessageSendDTO dingTalkDto = new MessageSendDTO();
            dingTalkDto.setChannel("dingtalk");
            dingTalkDto.setData(dingTalkPayload);
            messageService.sendMessage(dingTalkDto);
            log.info("钉钉消息推送完成");
        } catch (Exception e) {
            log.error("钉钉消息推送失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建钉钉消息数据（仅综合热榜）.
     */
    private DigestData buildDigestDataForDingTalk() {
        List<HotItem> top = fetchHotList("综合热榜", REBANG_TOP_URL, LIMIT);
        List<Section> sections = new ArrayList<>();
        sections.add(new Section("综合热榜", top));
        return new DigestData(sections);
    }

    private List<HotItem> fetchHotList(String source, String url, int limit) {
        try {
            String body = HttpUtil.get(url, 5000);
            JSONObject obj = JSON.parseObject(body);
            if (obj == null || obj.getInteger("code") == null || obj.getInteger("code") != 200) {
                log.warn("获取 {} 失败，code 非 200", source);
                return fallback(source, limit);
            }
            JSONObject data = obj.getJSONObject("data");
            Object rawList = data.get("list");
            JSONArray array;
            if (rawList instanceof String) {
                array = JSON.parseArray((String) rawList);
            } else if (rawList instanceof JSONArray) {
                array = (JSONArray) rawList;
            } else {
                log.warn("获取 {} 失败，list 结构未知", source);
                return fallback(source, limit);
            }
            List<HotItem> items = new ArrayList<>();
            for (int i = 0; i < array.size() && items.size() < limit; i++) {
                JSONObject item = array.getJSONObject(i);
                String title = item.getString("title");
                if ("百度贴吧热帖".equals(source) && isBlank(title)) {
                    title = item.getString("name");
                }
                String link = firstNonBlank(item.getString("www_url"), item.getString("mobile_url"), item.getString("url"));
                if (isBlank(link)) {
                    if ("哔哩哔哩热榜".equals(source)) {
                        String bvid = item.getString("bvid");
                        if (!isBlank(bvid)) {
                            link = "https://www.bilibili.com/video/" + bvid;
                        }
                    } else if ("抖音热榜".equals(source)) {
                        String awemeId = item.getString("aweme_id");
                        if (!isBlank(awemeId)) {
                            link = "https://www.douyin.com/video/" + awemeId;
                        }
                    } else if ("百度贴吧热帖".equals(source)) {
                        String topicId = item.getString("id");
                        if (!isBlank(topicId)) {
                            link = "https://tieba.baidu.com/hottopic/browse/hottopic?topic_id=" + topicId;
                        }
                    }
                }
                String heat = firstNonBlank(item.getString("heat_str"),
                        numberToHeat(item.getDouble("heat_num")),
                        item.getString("light_str"),
                        item.getString("reply_str"),
                        item.getString("label_str"));
                String desc = firstNonBlank(item.getString("desc"), item.getString("describe"));
                if (isBlank(title) || isBlank(link)) {
                    continue;
                }
                items.add(new HotItem(title, link, heat, desc));
            }
            if (!items.isEmpty()) {
                return items;
            }
        } catch (Exception e) {
            log.warn("获取 {} 失败，使用备用数据: {}", source, e.getMessage());
        }
        return fallback(source, limit);
    }

    private List<HotItem> fallback(String source, int limit) {
        return new ArrayList<HotItem>() {{
            add(new HotItem(source + " 暂不可用", "https://rebang.today", "稍后重试", null));
        }}.stream().limit(limit).collect(Collectors.toList());
    }

    private String numberToHeat(Double num) {
        if (num == null) {
            return null;
        }
        if (num >= 10000) {
            return String.format("%.1f 万热度", num / 10000);
        }
        return String.format("%.0f 热度", num);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * 渲染钉钉 Markdown 格式消息.
     */
    private String renderDingTalkMarkdown(DigestData digest, WeatherInfo weatherInfo, DailyQuote dailyQuote) {
        StringBuilder sb = new StringBuilder();
        
        LocalDateTime now = LocalDateTime.now();
        
        // 日期标题：📆 11月21日 周五
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        String weekDay = getWeekDayShort(now.getDayOfWeek());
        sb.append("## 📆 ").append(month).append("月").append(day).append("日 ").append(weekDay).append("\n\n");
        
        // 分隔线
        sb.append("---\n\n");
        
        // 天气信息
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
        
        // 每日一言
        sb.append("### 💭 每日一言\n\n");
        if (dailyQuote != null && !isBlank(dailyQuote.getQuote())) {
            sb.append("> ").append(dailyQuote.getQuote()).append("\n\n");
            if (!isBlank(dailyQuote.getFrom())) {
                sb.append("—— ").append(dailyQuote.getFrom()).append("\n\n");
            }
        } else {
            sb.append("每日一言获取中...\n\n");
        }
        
        // 分隔线
        sb.append("---\n\n");
        
        // 热点新闻
        for (Section section : digest.getSections()) {
            String icon = getSectionIcon(section.getName());
            sb.append("### ").append(icon).append(" ").append(section.getName()).append("\n\n");
            
            List<HotItem> items = section.getItems();
            if (items == null || items.isEmpty()) {
                sb.append("暂无数据\n\n");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    HotItem item = items.get(i);
                    // 使用更简洁的格式
                    sb.append("**").append(i + 1).append(". ").append(item.getTitle()).append("**\n");
                    
                    // 链接和热度信息（同一行，更紧凑）
                    boolean hasInfo = false;
                    if (!isBlank(item.getLink())) {
                        sb.append("[🔗 查看详情](").append(item.getLink()).append(")");
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
                    
                    // 描述信息（如果有且不太长，才显示）
                    if (!isBlank(item.getDesc()) && item.getDesc().length() <= 80) {
                        sb.append("_").append(item.getDesc()).append("_\n");
                    }
                    
                    // 每个条目之间空一行
                    if (i < items.size() - 1) {
                        sb.append("\n");
                    }
                }
            }
            sb.append("\n");
        }
        
        // 底部信息
        sb.append("---\n\n");
        sb.append("⭐ 由 小胖 自动推送\n");
        
        return sb.toString();
    }
    
    /**
     * 获取星期几的简短中文格式（周一、周二...）
     */
    private String getWeekDayShort(DayOfWeek dayOfWeek) {
        String[] weekDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weekDays[dayOfWeek.getValue() - 1];
    }
    
    /**
     * 根据榜单名称获取对应的图标
     */
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

