package com.pbad.schedule;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import com.pbad.schedule.domain.vo.HotItemVO;
import com.pbad.schedule.domain.vo.HotSectionVO;
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
import java.util.stream.Collectors;

/**
 * 每日热点新闻推送任务（Resend 邮箱渠道）。
 *
 * <p>内容：Rebang.Today 各榜单（综合、知乎、微博、虎扑、IT之家、豆瓣）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTechDigestTask {

    private static final String REBANG_TOP_URL = "https://api.rebang.today/v1/items?tab=top&sub_tab=lasthour&page=1&version=1";
    private static final String REBANG_ZHIHU_URL = "https://api.rebang.today/v1/items?tab=zhihu&date_type=now&page=1&version=1";
    private static final String REBANG_WEIBO_URL = "https://api.rebang.today/v1/items?tab=weibo&sub_tab=search&version=2";
    private static final String REBANG_HUPU_URL = "https://api.rebang.today/v1/items?tab=hupu&sub_tab=all-gambia&date_type=now&page=1&version=1";
    private static final String REBANG_XIAOHONGSHU_URL = "https://api.rebang.today/v1/items?tab=xiaohongshu&sub_tab=hot-search&page=1&version=1";
    private static final String REBANG_BILIBILI_URL = "https://api.rebang.today/v1/items?tab=bilibili&sub_tab=popular&date_type=now&page=1&version=1";
    private static final String REBANG_DOUYIN_URL = "https://api.rebang.today/v1/items?tab=douyin&date_type=now&page=1&version=1";
    private static final String REBANG_BAIDU_TIEBA_URL = "https://api.rebang.today/v1/items?tab=baidu-tieba&sub_tab=topic&page=1&version=1";

    private static final int LIMIT = 5;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MessageService messageService;

    /**
     * 每天 7:00、12:00、18:00 发送 Resend 邮箱热点新闻.
     */
    @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Shanghai")
    public void sendDailyDigest() {
        log.info("开始执行每日热点新闻任务（Resend 邮箱）");
        try {
            DigestData digestData = buildDigestData();
            String subject = "每日热点速览 | " + LocalDate.now().format(DATE_FMT);
            
            // 发送 Resend 邮箱
            String html = renderHtml(digestData);
            Map<String, Object> resendPayload = new HashMap<>();
            resendPayload.put("title", subject);
            resendPayload.put("html", html);
            MessageSendDTO resendDto = new MessageSendDTO();
            resendDto.setChannel("resend");
            resendDto.setData(resendPayload);
            messageService.sendMessage(resendDto);
            log.info("Resend 邮箱推送完成");
        } catch (Exception e) {
            log.error("每日热点新闻推送失败: {}", e.getMessage(), e);
        }
    }

    private DigestData buildDigestData() {
        List<HotItem> top = fetchHotList("综合热榜", REBANG_TOP_URL, LIMIT);
        List<HotItem> zhihu = fetchHotList("知乎热搜", REBANG_ZHIHU_URL, LIMIT);
        List<HotItem> weibo = fetchHotList("微博热搜", REBANG_WEIBO_URL, LIMIT);
        List<HotItem> hupu = fetchHotList("虎扑热帖", REBANG_HUPU_URL, LIMIT);
        List<HotItem> xiaohongshu = fetchHotList("小红书热帖", REBANG_XIAOHONGSHU_URL, LIMIT);
        List<HotItem> bilibili = fetchHotList("哔哩哔哩热榜", REBANG_BILIBILI_URL, LIMIT);
        List<HotItem> douyin = fetchHotList("抖音热榜", REBANG_DOUYIN_URL, LIMIT);
        List<HotItem> baiduTieba = fetchHotList("百度贴吧热帖", REBANG_BAIDU_TIEBA_URL, LIMIT);
        List<Section> sections = new ArrayList<>();
        sections.add(new Section("综合热榜", top));
        sections.add(new Section("知乎热搜", zhihu));
        sections.add(new Section("微博热搜", weibo));
        sections.add(new Section("虎扑热帖", hupu));
        sections.add(new Section("小红书热帖", xiaohongshu));
        sections.add(new Section("哔哩哔哩热榜", bilibili));
        sections.add(new Section("抖音热榜", douyin));
        sections.add(new Section("百度贴吧热帖", baiduTieba));
        return new DigestData(sections);
    }

    /**
     * 获取热点数据（供 API 调用）
     *
     * @return 热点板块列表
     */
    public List<HotSectionVO> getHotSections() {
        List<HotItem> top = fetchHotList("综合热榜", REBANG_TOP_URL, LIMIT);
        List<HotItem> zhihu = fetchHotList("知乎热搜", REBANG_ZHIHU_URL, LIMIT);
        List<HotItem> weibo = fetchHotList("微博热搜", REBANG_WEIBO_URL, LIMIT);
        List<HotItem> hupu = fetchHotList("虎扑热帖", REBANG_HUPU_URL, LIMIT);
        List<HotItem> xiaohongshu = fetchHotList("小红书热帖", REBANG_XIAOHONGSHU_URL, LIMIT);
        List<HotItem> bilibili = fetchHotList("哔哩哔哩热榜", REBANG_BILIBILI_URL, LIMIT);
        List<HotItem> douyin = fetchHotList("抖音热榜", REBANG_DOUYIN_URL, LIMIT);
        List<HotItem> baiduTieba = fetchHotList("百度贴吧热帖", REBANG_BAIDU_TIEBA_URL, LIMIT);
        
        List<HotSectionVO> sections = new ArrayList<>();
        sections.add(convertToSectionVO("综合热榜", top));
        sections.add(convertToSectionVO("知乎热搜", zhihu));
        sections.add(convertToSectionVO("微博热搜", weibo));
        sections.add(convertToSectionVO("虎扑热帖", hupu));
        sections.add(convertToSectionVO("小红书热帖", xiaohongshu));
        sections.add(convertToSectionVO("哔哩哔哩热榜", bilibili));
        sections.add(convertToSectionVO("抖音热榜", douyin));
        sections.add(convertToSectionVO("百度贴吧热帖", baiduTieba));
        return sections;
    }

    /**
     * 将内部 HotItem 转换为 VO
     */
    private HotSectionVO convertToSectionVO(String name, List<HotItem> items) {
        HotSectionVO sectionVO = new HotSectionVO();
        sectionVO.setName(name);
        List<HotItemVO> itemVOs = items.stream().map(item -> {
            HotItemVO vo = new HotItemVO();
            vo.setTitle(item.getTitle());
            vo.setLink(item.getLink());
            vo.setHeat(item.getHeat());
            vo.setDesc(item.getDesc());
            return vo;
        }).collect(Collectors.toList());
        sectionVO.setItems(itemVOs);
        return sectionVO;
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
                sb.append("<div class=\"card\">")
                        .append("<div class=\"title\"><a href=\"").append(item.getLink()).append("\" target=\"_blank\">")
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

