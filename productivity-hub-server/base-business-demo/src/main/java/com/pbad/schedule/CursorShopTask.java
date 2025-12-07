package com.pbad.schedule;

import com.pbad.messages.service.MessageChannelService;
import com.pbad.tools.domain.vo.CursorCommodityVO;
import com.pbad.tools.service.CursorShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cursor 邮箱自助小店定时任务.
 *
 * <p>逻辑：按固定时间窗口（工作日 9-18 点整点，11-15 禁止，周末禁用）拉取商品信息，整理后通过钉钉机器人推送。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CursorShopTask {

    @Qualifier("dingtalkChannelService")
    private final MessageChannelService dingtalkChannelService;
    private final CursorShopService cursorShopService;

    /**
     * 供调度器调用的执行入口.
     */
    public void execute() {
        log.info("开始执行 Cursor 邮箱自助小店任务");
        List<CursorCommodityVO> commodities = cursorShopService.fetchCommodities();
        if (commodities.isEmpty()) {
            log.info("未获取到商品数据，跳过推送");
            return;
        }
        String markdown = buildMarkdown(commodities);
        Map<String, Object> payload = new HashMap<>();
        payload.put("msgType", "markdown");
        payload.put("content", markdown);

        dingtalkChannelService.sendMessage(payload);
    }

    /**
     * 计算下一次允许执行的时间（整点）。
     */
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
            // 跳过周末
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

