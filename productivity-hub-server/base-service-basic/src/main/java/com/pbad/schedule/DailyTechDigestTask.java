package com.pbad.schedule;

import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import com.pbad.thirdparty.api.HotDataApi;
import com.pbad.thirdparty.api.DailyQuoteApi;
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

    @Scheduled(cron = "0 0 7,12,18 * * ?", zone = "Asia/Shanghai")
    public void sendDailyDigest() {
        log.info("开始执行每日热点新闻任务（Resend 邮箱）");
        try {
            DigestData digestData = buildDigestData();
            String subject = "每日热点速览 | " + LocalDate.now().format(DATE_FMT);

            String html = renderHtml(digestData);
            Map<String, Object> resendPayload = new HashMap<>();
            resendPayload.put("title", subject);
            resendPayload.put("html", html);
            MessageSendDTO resendDto = new MessageSendDTO();
            resendDto.setChannel("resend");
            resendDto.setData(resendPayload);
            messageService.sendMessage(resendDto, "system");
            log.info("Resend 邮箱推送完成");
        } catch (Exception e) {
            log.error("每日热点新闻推送失败: {}", e.getMessage(), e);
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


