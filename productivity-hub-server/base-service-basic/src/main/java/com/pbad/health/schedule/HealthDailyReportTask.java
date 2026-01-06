package com.pbad.health.schedule;

import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.service.ConfigService;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.mapper.*;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 用户健康统计邮件推送定时任务（周报）.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthDailyReportTask {

    private final HealthUserBodyInfoMapper userBodyInfoMapper;
    private final HealthExerciseRecordMapper exerciseRecordMapper;
    private final HealthWaterIntakeMapper waterIntakeMapper;
    private final HealthWaterTargetMapper waterTargetMapper;
    private final HealthWeightRecordMapper weightRecordMapper;
    private final MessageService messageService;
    private final ConfigService configService;
    private final UserRoleUtil userRoleUtil;
    private final UserMapper userMapper;

    // 默认每日目标饮水量（毫升）
    private static final int DEFAULT_DAILY_TARGET_ML = 2000;

    /**
     * 每周一7:00执行，推送上周健康统计邮件
     */
    @Scheduled(cron = "0 0 7 ? * MON", zone = "Asia/Shanghai")
    public void sendWeeklyHealthReport() {
        sendWeeklyHealthReport(null);
    }

    /**
     * 发送健康统计周报邮件
     * 
     * @param targetUserId 目标用户ID，如果为null则推送给所有开启了定时任务的用户
     */
    public void sendWeeklyHealthReport(String targetUserId) {
        if (!isTaskEnabled()) {
            log.info("用户健康统计周报邮件推送任务已被关闭，跳过执行");
            return;
        }

        log.info("开始执行用户健康统计周报邮件推送任务");

        try {
            // 先获取开启了定时任务的用户
            List<UserPO> enabledUsers = getEnabledUsers("healthDailyReport.enabled", targetUserId);
            if (enabledUsers == null || enabledUsers.isEmpty()) {
                log.info("没有开启了定时任务的用户，跳过执行");
                return;
            }

            // 计算上周日期范围（上周一到上周日）
            LocalDate today = LocalDate.now();
            LocalDate lastMonday = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
            LocalDate lastSunday = lastMonday.plusDays(6);
            Date weekStart = Date.from(lastMonday.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date weekEnd = Date.from(lastSunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            int successCount = 0;
            int failCount = 0;

            for (UserPO user : enabledUsers) {
                String userId = user.getId();
                
                // 跳过超级管理员（用户名是 admin）
                if (userRoleUtil.isSuperAdmin(userId)) {
                    log.debug("用户 {} 是超级管理员，跳过推送", userId);
                    continue;
                }
                
                // 查询用户的健康信息（需要是用户）
                HealthUserBodyInfoPO healthUser = userBodyInfoMapper.selectByUserId(userId);
                if (healthUser == null) {
                    log.debug("用户 {} 未配置健康信息，跳过", userId);
                    continue;
                }
                
                try {
                    sendReportToUser(healthUser, lastMonday, lastSunday, weekStart, weekEnd);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("向用户 {} 发送健康统计周报邮件失败: {}", userId, e.getMessage(), e);
                }
            }

            log.info("用户健康统计周报邮件推送任务执行完成，成功：{}，失败：{}", successCount, failCount);
        } catch (Exception e) {
            log.error("用户每日健康统计邮件推送任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 向指定用户发送健康统计周报邮件
     */
    private void sendReportToUser(HealthUserBodyInfoPO user, LocalDate weekStartDate, LocalDate weekEndDate, Date startDate, Date endDate) {
        String userId = user.getUserId();
        
        // 从用户全局配置中读取 resend 邮箱地址
        String email = configService.getConfigValue("resend", "resend.toEmail", userId);
        
        if (email == null || email.trim().isEmpty()) {
            log.debug("用户 {} 未在全局配置中配置Resend邮箱地址，跳过推送", userId);
            return;
        }

        // 统计运动数据
        Map<String, Object> exerciseStats = exerciseRecordMapper.statisticsByDateRange(userId, startDate, endDate);
        if (exerciseStats == null) {
            exerciseStats = new HashMap<>();
        }
        log.debug("用户 {} 运动统计数据: {}", userId, exerciseStats);

        // 统计饮水数据
        Map<String, Object> waterStats = waterIntakeMapper.statisticsByDateRange(userId, startDate, endDate);
        if (waterStats == null) {
            waterStats = new HashMap<>();
        }
        log.debug("用户 {} 饮水统计数据: {}", userId, waterStats);

        // 获取饮水目标（从用户配置中获取，如果没有则使用默认值）
        // 周报需要计算整周的目标饮水量（每日目标 * 7天）
        int dailyTargetMl = DEFAULT_DAILY_TARGET_ML;
        com.pbad.health.domain.po.HealthWaterTargetPO waterTarget = waterTargetMapper.selectByUserId(userId);
        if (waterTarget != null && waterTarget.getDailyTargetMl() != null) {
            dailyTargetMl = waterTarget.getDailyTargetMl();
        }
        int targetMl = dailyTargetMl * 7; // 周目标 = 每日目标 * 7
        log.debug("用户 {} 每日饮水目标: {} ml, 周目标: {} ml", userId, dailyTargetMl, targetMl);
        
        // 统计日期范围内的总饮水量
        Integer consumedMl = waterIntakeMapper.sumVolumeByDateRange(userId, startDate, endDate);
        consumedMl = consumedMl != null ? consumedMl : 0;
        log.debug("用户 {} 已饮水量: {} ml (日期范围: {} 到 {})", userId, consumedMl, startDate, endDate);
        boolean waterAchieved = consumedMl >= targetMl;
        double waterProgress = targetMl > 0 ? (double) consumedMl / targetMl * 100.0 : 0.0;

        // 查询体重记录（昨日）
        List<Map<String, Object>> weightTrend = weightRecordMapper.queryTrendData(userId, startDate, endDate);
        BigDecimal latestWeight = null;
        if (weightTrend != null && !weightTrend.isEmpty()) {
            Object weightObj = weightTrend.get(weightTrend.size() - 1).get("weightKg");
            if (weightObj == null) {
                weightObj = weightTrend.get(weightTrend.size() - 1).get("value");
            }
            if (weightObj instanceof BigDecimal) {
                latestWeight = (BigDecimal) weightObj;
            } else if (weightObj instanceof Number) {
                latestWeight = BigDecimal.valueOf(((Number) weightObj).doubleValue());
            }
        }
        log.debug("用户 {} 体重趋势数据: {} 条, 最新体重: {}", userId, weightTrend != null ? weightTrend.size() : 0, latestWeight);

        // 查询体重统计数据（用于显示平均、最高、最低体重）
        Map<String, Object> weightStats = weightRecordMapper.statisticsByDateRange(userId, startDate, endDate);
        if (weightStats == null) {
            weightStats = new HashMap<>();
        }
        log.debug("用户 {} 体重统计数据: {}", userId, weightStats);

        // 查询趋势数据（最近30天，用于生成趋势图）
        LocalDate trendEndDate = weekEndDate;
        LocalDate trendStartDate = trendEndDate.minusDays(29);
        Date trendStart = Date.from(trendStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date trendEnd = Date.from(trendEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        // 查询运动趋势数据（最近30天）
        List<Map<String, Object>> exerciseTrendData = exerciseRecordMapper.queryTrendData(userId, trendStart, trendEnd, "duration");
        log.info("用户 {} 运动趋势数据: {} 条 (日期范围: {} 到 {})", userId, exerciseTrendData != null ? exerciseTrendData.size() : 0, trendStartDate, trendEndDate);
        if (exerciseTrendData != null && !exerciseTrendData.isEmpty()) {
            log.info("用户 {} 运动趋势数据示例: {}", userId, exerciseTrendData.get(0));
        }
        
        // 查询体重趋势数据（最近30天）
        List<Map<String, Object>> weightTrendData = weightRecordMapper.queryTrendData(userId, trendStart, trendEnd);
        log.info("用户 {} 体重趋势数据: {} 条 (日期范围: {} 到 {})", userId, weightTrendData != null ? weightTrendData.size() : 0, trendStartDate, trendEndDate);
        if (weightTrendData != null && !weightTrendData.isEmpty()) {
            log.info("用户 {} 体重趋势数据示例: {}", userId, weightTrendData.get(0));
        }
        
        // 查询饮水趋势数据（最近30天）
        List<Map<String, Object>> waterTrendData = waterIntakeMapper.queryTrendData(userId, trendStart, trendEnd);
        log.info("用户 {} 饮水趋势数据: {} 条 (日期范围: {} 到 {})", userId, waterTrendData != null ? waterTrendData.size() : 0, trendStartDate, trendEndDate);
        if (waterTrendData != null && !waterTrendData.isEmpty()) {
            log.info("用户 {} 饮水趋势数据示例: {}", userId, waterTrendData.get(0));
        }

        // 构建邮件内容
        String dateRange = weekStartDate.toString() + " 至 " + weekEndDate.toString();
        String subject = "健康统计周报 - " + dateRange;
        String html = buildReportHtml(user, weekStartDate, weekEndDate, exerciseStats, waterStats, weightStats,
                consumedMl, targetMl, dailyTargetMl, waterAchieved, waterProgress, latestWeight,
                exerciseTrendData, weightTrendData, waterTrendData);

        // 发送邮件
        Map<String, Object> resendPayload = new HashMap<>();
        resendPayload.put("to", email);
        resendPayload.put("title", subject);
        resendPayload.put("html", html);

        MessageSendDTO messageDto = new MessageSendDTO();
        messageDto.setChannel("resend");
        messageDto.setData(resendPayload);
        messageService.sendMessage(messageDto, userId);

        log.debug("已向用户 {} 发送健康统计周报邮件", userId);
    }

    /**
     * 构建邮件HTML内容
     */
    private String buildReportHtml(HealthUserBodyInfoPO user, LocalDate weekStartDate, LocalDate weekEndDate,
                                    Map<String, Object> exerciseStats, Map<String, Object> waterStats,
                                    Map<String, Object> weightStats, int consumedMl, int targetMl, int dailyTargetMl,
                                    boolean waterAchieved, double waterProgress, BigDecimal latestWeight,
                                    List<Map<String, Object>> exerciseTrendData,
                                    List<Map<String, Object>> weightTrendData,
                                    List<Map<String, Object>> waterTrendData) {
        StringBuilder sb = new StringBuilder();

        // HTML头部
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html>\n<head>\n");
        sb.append("<meta charset=\"UTF-8\">\n");
        sb.append("<style>\n");
        sb.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n");
        sb.append(".container { max-width: 800px; margin: 0 auto; padding: 20px; }\n");
        sb.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px; }\n");
        sb.append(".header h1 { margin: 0; font-size: 28px; }\n");
        sb.append(".header p { margin: 10px 0 0 0; opacity: 0.9; }\n");
        sb.append(".section { background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px; }\n");
        sb.append(".section h2 { margin-top: 0; color: #667eea; border-bottom: 2px solid #667eea; padding-bottom: 10px; }\n");
        sb.append(".stat-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-top: 15px; }\n");
        sb.append(".stat-item { background: white; padding: 15px; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        sb.append(".stat-label { font-size: 14px; color: #666; margin-bottom: 5px; }\n");
        sb.append(".stat-value { font-size: 24px; font-weight: bold; color: #333; }\n");
        sb.append(".progress-bar { background: #e0e0e0; border-radius: 10px; height: 20px; margin: 10px 0; overflow: hidden; }\n");
        sb.append(".progress-fill { background: linear-gradient(90deg, #667eea 0%, #764ba2 100%); height: 100%; transition: width 0.3s; }\n");
        sb.append(".progress-text { text-align: center; margin-top: 5px; font-size: 14px; color: #666; }\n");
        sb.append(".table { width: 100%; border-collapse: collapse; margin-top: 15px; }\n");
        sb.append(".table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        sb.append(".table th { background: #667eea; color: white; }\n");
        sb.append(".table tr:hover { background: #f5f5f5; }\n");
        sb.append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }\n");
        sb.append("</style>\n");
        sb.append("</head>\n<body>\n");
        sb.append("<div class=\"container\">\n");

        // 头部
        String dateRange = weekStartDate.toString() + " 至 " + weekEndDate.toString();
        sb.append("<div class=\"header\">\n");
        sb.append("<h1>📊 健康统计周报</h1>\n");
        sb.append("<p>").append(dateRange).append(" 健康数据汇总</p>\n");
        sb.append("</div>\n");

        // 运动统计
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>🏃 运动统计</h2>\n");
        sb.append("<div class=\"stat-grid\">\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">运动次数</div>\n");
        sb.append("<div class=\"stat-value\">").append(getIntValue(exerciseStats, "totalCount", 0)).append(" 次</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">总时长</div>\n");
        sb.append("<div class=\"stat-value\">").append(getIntValue(exerciseStats, "totalDuration", 0)).append(" 分钟</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">消耗卡路里</div>\n");
        sb.append("<div class=\"stat-value\">").append(getIntValue(exerciseStats, "totalCalories", 0)).append(" 卡</div>\n");
        sb.append("</div>\n");
        if (getBigDecimalValue(exerciseStats, "totalDistance", BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            sb.append("<div class=\"stat-item\">\n");
            sb.append("<div class=\"stat-label\">总距离</div>\n");
            sb.append("<div class=\"stat-value\">").append(getBigDecimalValue(exerciseStats, "totalDistance", BigDecimal.ZERO)).append(" 公里</div>\n");
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        sb.append("</div>\n");

        // 饮水统计
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>💧 饮水统计</h2>\n");
        sb.append("<div class=\"stat-grid\">\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">已饮水量</div>\n");
        sb.append("<div class=\"stat-value\">").append(consumedMl).append(" ml</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">周目标饮水量</div>\n");
        sb.append("<div class=\"stat-value\">").append(targetMl).append(" ml</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">日均目标</div>\n");
        sb.append("<div class=\"stat-value\">").append(dailyTargetMl).append(" ml</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">完成进度</div>\n");
        sb.append("<div class=\"stat-value\">").append(String.format("%.1f", waterProgress)).append("%</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"stat-item\">\n");
        sb.append("<div class=\"stat-label\">达标状态</div>\n");
        sb.append("<div class=\"stat-value\">").append(waterAchieved ? "✅ 已达标" : "❌ 未达标").append("</div>\n");
        sb.append("</div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"progress-bar\">\n");
        sb.append("<div class=\"progress-fill\" style=\"width: ").append(Math.min(100, waterProgress)).append("%;\"></div>\n");
        sb.append("</div>\n");
        sb.append("<div class=\"progress-text\">").append(consumedMl).append(" / ").append(targetMl).append(" ml</div>\n");
        sb.append("</div>\n");

        // 体重统计
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>⚖️ 体重统计</h2>\n");
        sb.append("<div class=\"stat-grid\">\n");
        if (latestWeight != null) {
            sb.append("<div class=\"stat-item\">\n");
            sb.append("<div class=\"stat-label\">最新体重</div>\n");
            sb.append("<div class=\"stat-value\">").append(latestWeight.setScale(1, RoundingMode.HALF_UP)).append(" kg</div>\n");
            sb.append("</div>\n");
            
            BigDecimal avgWeight = getBigDecimalValue(weightStats, "avgWeight", null);
            if (avgWeight != null) {
                sb.append("<div class=\"stat-item\">\n");
                sb.append("<div class=\"stat-label\">平均体重</div>\n");
                sb.append("<div class=\"stat-value\">").append(avgWeight.setScale(1, RoundingMode.HALF_UP)).append(" kg</div>\n");
                sb.append("</div>\n");
            }
            
            BigDecimal maxWeight = getBigDecimalValue(weightStats, "maxWeight", null);
            if (maxWeight != null) {
                sb.append("<div class=\"stat-item\">\n");
                sb.append("<div class=\"stat-label\">最高体重</div>\n");
                sb.append("<div class=\"stat-value\">").append(maxWeight.setScale(1, RoundingMode.HALF_UP)).append(" kg</div>\n");
                sb.append("</div>\n");
            }
            
            BigDecimal minWeight = getBigDecimalValue(weightStats, "minWeight", null);
            if (minWeight != null) {
                sb.append("<div class=\"stat-item\">\n");
                sb.append("<div class=\"stat-label\">最低体重</div>\n");
                sb.append("<div class=\"stat-value\">").append(minWeight.setScale(1, RoundingMode.HALF_UP)).append(" kg</div>\n");
                sb.append("</div>\n");
            }
            
            if (user.getTargetWeightKg() != null) {
                BigDecimal gap = latestWeight.subtract(user.getTargetWeightKg());
                sb.append("<div class=\"stat-item\">\n");
                sb.append("<div class=\"stat-label\">目标体重</div>\n");
                sb.append("<div class=\"stat-value\">").append(user.getTargetWeightKg()).append(" kg</div>\n");
                sb.append("</div>\n");
                sb.append("<div class=\"stat-item\">\n");
                sb.append("<div class=\"stat-label\">差距</div>\n");
                sb.append("<div class=\"stat-value\">").append(gap.compareTo(BigDecimal.ZERO) > 0 ? "+" : "").append(gap.setScale(1, RoundingMode.HALF_UP)).append(" kg</div>\n");
                sb.append("</div>\n");
            }
        } else {
            sb.append("<div class=\"stat-item\">\n");
            sb.append("<div class=\"stat-label\">暂无体重数据</div>\n");
            sb.append("</div>\n");
        }
        sb.append("</div>\n");
        sb.append("</div>\n");

        // 运动统计趋势图
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>📈 运动统计趋势图（最近30天）</h2>\n");
        if (exerciseTrendData != null && !exerciseTrendData.isEmpty()) {
            sb.append(generateExerciseTrendChart(exerciseTrendData));
        } else {
            sb.append("<p style=\"text-align: center; color: #999; padding: 40px;\">暂无运动趋势数据</p>\n");
        }
        sb.append("</div>\n");

        // 体重统计趋势图
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>📈 体重统计趋势图（最近30天）</h2>\n");
        if (weightTrendData != null && !weightTrendData.isEmpty()) {
            sb.append(generateWeightTrendChart(weightTrendData, user.getTargetWeightKg()));
        } else {
            sb.append("<p style=\"text-align: center; color: #999; padding: 40px;\">暂无体重趋势数据</p>\n");
        }
        sb.append("</div>\n");

        // 饮水统计趋势图
        sb.append("<div class=\"section\">\n");
        sb.append("<h2>📈 饮水统计趋势图（最近30天）</h2>\n");
        if (waterTrendData != null && !waterTrendData.isEmpty()) {
            sb.append(generateWaterTrendChart(waterTrendData, dailyTargetMl));
        } else {
            sb.append("<p style=\"text-align: center; color: #999; padding: 40px;\">暂无饮水趋势数据</p>\n");
        }
        sb.append("</div>\n");

        // 底部
        sb.append("<div class=\"footer\">\n");
        sb.append("<p>由 Productivity Hub 自动生成 | ").append(dateRange).append("</p>\n");
        sb.append("</div>\n");

        sb.append("</div>\n");
        sb.append("</body>\n</html>");

        return sb.toString();
    }

    /**
     * 生成运动趋势图（SVG格式）
     */
    private String generateExerciseTrendChart(List<Map<String, Object>> trendData) {
        if (trendData == null || trendData.isEmpty()) {
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        int width = 700;
        int height = 300;
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // 提取数据
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int maxValue = 0;
        for (Map<String, Object> item : trendData) {
            String date = (String) item.get("date");
            if (date == null || date.trim().isEmpty()) {
                log.warn("运动趋势数据中存在日期为空的记录，跳过: {}", item);
                continue;
            }
            Object valueObj = item.get("value");
            int value = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;
            dates.add(date);
            values.add(value);
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        if (dates.isEmpty()) {
            log.warn("运动趋势数据提取后为空，原始数据: {}", trendData);
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        if (maxValue == 0) {
            maxValue = 100; // 避免除零
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // 背景
        svg.append("<rect width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\"#ffffff\"/>\n");
        
        // 绘制网格线
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = padding + (chartHeight * i / gridLines);
            svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(y)
                .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(y)
                .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
            
            // Y轴标签
            int labelValue = maxValue - (maxValue * i / gridLines);
            svg.append("<text x=\"").append(padding - 10).append("\" y=\"").append(y + 5)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"end\">").append(labelValue).append("</text>\n");
        }

        // 绘制折线
        if (dates.size() > 1) {
            svg.append("<polyline points=\"");
            int divisor = dates.size() > 1 ? dates.size() - 1 : 1;
            for (int i = 0; i < dates.size(); i++) {
                int x = padding + (chartWidth * i / divisor);
                int y = padding + chartHeight - (values.get(i) * chartHeight / maxValue);
                svg.append(x).append(",").append(y);
                if (i < dates.size() - 1) {
                    svg.append(" ");
                }
            }
            svg.append("\" fill=\"none\" stroke=\"#667eea\" stroke-width=\"2\"/>\n");

            // 绘制数据点
            for (int i = 0; i < dates.size(); i++) {
                int x = padding + (chartWidth * i / divisor);
                int y = padding + chartHeight - (values.get(i) * chartHeight / maxValue);
                svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                    .append("\" r=\"3\" fill=\"#667eea\"/>\n");
            }
        } else if (dates.size() == 1) {
            // 只有一个数据点时，绘制一个点
            int x = padding + chartWidth / 2;
            int y = padding + chartHeight - (values.get(0) * chartHeight / maxValue);
            svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                .append("\" r=\"5\" fill=\"#667eea\"/>\n");
        }

        // X轴标签（只显示部分日期，避免拥挤）
        int labelInterval = Math.max(1, dates.size() / 10);
        int divisor = dates.size() > 1 ? dates.size() - 1 : 1;
        for (int i = 0; i < dates.size(); i += labelInterval) {
            int x = padding + (chartWidth * i / divisor);
            String dateLabel = dates.get(i).substring(5); // 只显示月-日
            svg.append("<text x=\"").append(x).append("\" y=\"").append(height - padding + 20)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"middle\">").append(dateLabel).append("</text>\n");
        }

        svg.append("</svg>");
        String svgString = svg.toString();
        // 将SVG转换为Base64编码的data URI，以便在邮件中显示
        try {
            String base64Svg = Base64.getEncoder().encodeToString(svgString.getBytes(StandardCharsets.UTF_8));
            return "<div style=\"text-align: center; margin: 20px 0; padding: 10px;\">\n" +
                   "<img src=\"data:image/svg+xml;base64," + base64Svg + "\" alt=\"运动趋势图\" " +
                   "style=\"max-width: 100%; width: 700px; height: auto; display: block; margin: 0 auto; border: 1px solid #e0e0e0;\"/>\n" +
                   "</div>";
        } catch (Exception e) {
            log.error("编码SVG失败", e);
            return "<p style=\"text-align: center; color: #999;\">图表生成失败</p>";
        }
    }

    /**
     * 生成体重趋势图（SVG格式）
     */
    private String generateWeightTrendChart(List<Map<String, Object>> trendData, BigDecimal targetWeight) {
        if (trendData == null || trendData.isEmpty()) {
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        int width = 700;
        int height = 300;
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // 提取数据
        List<String> dates = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        BigDecimal minWeight = null;
        BigDecimal maxWeight = null;
        for (Map<String, Object> item : trendData) {
            String date = (String) item.get("date");
            if (date == null || date.trim().isEmpty()) {
                log.warn("体重趋势数据中存在日期为空的记录，跳过: {}", item);
                continue;
            }
            Object weightObj = item.get("weightKg");
            BigDecimal weight = null;
            if (weightObj instanceof BigDecimal) {
                weight = (BigDecimal) weightObj;
            } else if (weightObj instanceof Number) {
                weight = BigDecimal.valueOf(((Number) weightObj).doubleValue());
            }
            if (weight != null) {
                dates.add(date);
                weights.add(weight);
                if (minWeight == null || weight.compareTo(minWeight) < 0) {
                    minWeight = weight;
                }
                if (maxWeight == null || weight.compareTo(maxWeight) > 0) {
                    maxWeight = weight;
                }
            } else {
                log.warn("体重趋势数据中存在体重为空的记录，跳过: {}", item);
            }
        }
        
        if (dates.isEmpty()) {
            log.warn("体重趋势数据提取后为空，原始数据: {}", trendData);
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        if (minWeight == null || maxWeight == null) {
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        // 扩展范围以便显示目标线
        if (targetWeight != null) {
            if (targetWeight.compareTo(minWeight) < 0) {
                minWeight = targetWeight;
            }
            if (targetWeight.compareTo(maxWeight) > 0) {
                maxWeight = targetWeight;
            }
        }

        BigDecimal range = maxWeight.subtract(minWeight);
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            range = BigDecimal.valueOf(5); // 避免除零
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // 背景
        svg.append("<rect width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\"#ffffff\"/>\n");
        
        // 绘制目标线
        if (targetWeight != null) {
            int targetY = padding + chartHeight - (targetWeight.subtract(minWeight).divide(range, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(chartHeight))).intValue();
            svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(targetY)
                .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(targetY)
                .append("\" stroke=\"#67C23A\" stroke-width=\"2\" stroke-dasharray=\"5,5\"/>\n");
            svg.append("<text x=\"").append(width - padding + 10).append("\" y=\"").append(targetY + 5)
                .append("\" font-size=\"10\" fill=\"#67C23A\">目标: ").append(targetWeight).append("kg</text>\n");
        }

        // 绘制网格线
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = padding + (chartHeight * i / gridLines);
            svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(y)
                .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(y)
                .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
            
            // Y轴标签
            BigDecimal labelValue = maxWeight.subtract(range.multiply(BigDecimal.valueOf(i)).divide(BigDecimal.valueOf(gridLines), 2, RoundingMode.HALF_UP));
            svg.append("<text x=\"").append(padding - 10).append("\" y=\"").append(y + 5)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"end\">").append(labelValue.setScale(1, RoundingMode.HALF_UP)).append("</text>\n");
        }

        // 绘制折线
        int divisor = dates.size() > 1 ? dates.size() - 1 : 1;
        if (dates.size() > 1) {
            svg.append("<polyline points=\"");
            for (int i = 0; i < dates.size(); i++) {
                int x = padding + (chartWidth * i / divisor);
                BigDecimal weight = weights.get(i);
                int y = padding + chartHeight - (weight.subtract(minWeight).divide(range, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(chartHeight))).intValue();
                svg.append(x).append(",").append(y);
                if (i < dates.size() - 1) {
                    svg.append(" ");
                }
            }
            svg.append("\" fill=\"none\" stroke=\"#E6A23C\" stroke-width=\"2\"/>\n");

            // 绘制数据点
            for (int i = 0; i < dates.size(); i++) {
                int x = padding + (chartWidth * i / divisor);
                BigDecimal weight = weights.get(i);
                int y = padding + chartHeight - (weight.subtract(minWeight).divide(range, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(chartHeight))).intValue();
                svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                    .append("\" r=\"3\" fill=\"#E6A23C\"/>\n");
            }
        } else if (dates.size() == 1) {
            // 只有一个数据点时，绘制一个点
            int x = padding + chartWidth / 2;
            BigDecimal weight = weights.get(0);
            int y = padding + chartHeight - (weight.subtract(minWeight).divide(range, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(chartHeight))).intValue();
            svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                .append("\" r=\"5\" fill=\"#E6A23C\"/>\n");
        }

        // X轴标签
        int labelInterval = Math.max(1, dates.size() / 10);
        for (int i = 0; i < dates.size(); i += labelInterval) {
            int x = padding + (chartWidth * i / (dates.size() - 1));
            String dateLabel = dates.get(i).substring(5); // 只显示月-日
            svg.append("<text x=\"").append(x).append("\" y=\"").append(height - padding + 20)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"middle\">").append(dateLabel).append("</text>\n");
        }

        svg.append("</svg>");
        String svgString = svg.toString();
        // 将SVG转换为Base64编码的data URI，以便在邮件中显示
        try {
            String base64Svg = Base64.getEncoder().encodeToString(svgString.getBytes(StandardCharsets.UTF_8));
            return "<div style=\"text-align: center; margin: 20px 0; padding: 10px;\">\n" +
                   "<img src=\"data:image/svg+xml;base64," + base64Svg + "\" alt=\"体重趋势图\" " +
                   "style=\"max-width: 100%; width: 700px; height: auto; display: block; margin: 0 auto; border: 1px solid #e0e0e0;\"/>\n" +
                   "</div>";
        } catch (Exception e) {
            log.error("编码SVG失败", e);
            return "<p style=\"text-align: center; color: #999;\">图表生成失败</p>";
        }
    }

    /**
     * 生成饮水趋势图（SVG格式）
     */
    private String generateWaterTrendChart(List<Map<String, Object>> trendData, int dailyTargetMl) {
        if (trendData == null || trendData.isEmpty()) {
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        int width = 700;
        int height = 300;
        int padding = 50;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // 提取数据
        List<String> dates = new ArrayList<>();
        List<Integer> volumes = new ArrayList<>();
        int maxVolume = dailyTargetMl;
        for (Map<String, Object> item : trendData) {
            String date = (String) item.get("date");
            if (date == null || date.trim().isEmpty()) {
                log.warn("饮水趋势数据中存在日期为空的记录，跳过: {}", item);
                continue;
            }
            Object volumeObj = item.get("totalVolume");
            int volume = volumeObj instanceof Number ? ((Number) volumeObj).intValue() : 0;
            dates.add(date);
            volumes.add(volume);
            if (volume > maxVolume) {
                maxVolume = volume;
            }
        }
        
        if (dates.isEmpty()) {
            log.warn("饮水趋势数据提取后为空，原始数据: {}", trendData);
            return "<p style=\"text-align: center; color: #999;\">暂无数据</p>";
        }

        if (maxVolume == 0) {
            maxVolume = dailyTargetMl; // 使用目标值作为最大值
        }

        StringBuilder svg = new StringBuilder();
        svg.append("<svg width=\"").append(width).append("\" height=\"").append(height).append("\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        
        // 背景
        svg.append("<rect width=\"").append(width).append("\" height=\"").append(height).append("\" fill=\"#ffffff\"/>\n");
        
        // 绘制目标线（使用每日目标，因为趋势图是按天显示的）
        int dailyTargetY = padding + chartHeight - (dailyTargetMl * chartHeight / maxVolume);
        svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(dailyTargetY)
            .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(dailyTargetY)
            .append("\" stroke=\"#67C23A\" stroke-width=\"2\" stroke-dasharray=\"5,5\"/>\n");
        svg.append("<text x=\"").append(width - padding + 10).append("\" y=\"").append(dailyTargetY + 5)
            .append("\" font-size=\"10\" fill=\"#67C23A\">目标: ").append(dailyTargetMl).append("ml</text>\n");

        // 绘制网格线
        int gridLines = 5;
        for (int i = 0; i <= gridLines; i++) {
            int y = padding + (chartHeight * i / gridLines);
            svg.append("<line x1=\"").append(padding).append("\" y1=\"").append(y)
                .append("\" x2=\"").append(width - padding).append("\" y2=\"").append(y)
                .append("\" stroke=\"#e0e0e0\" stroke-width=\"1\"/>\n");
            
            // Y轴标签
            int labelValue = maxVolume - (maxVolume * i / gridLines);
            svg.append("<text x=\"").append(padding - 10).append("\" y=\"").append(y + 5)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"end\">").append(labelValue).append("</text>\n");
        }

        // 绘制柱状图
        if (dates.size() > 0) {
            int barWidth = chartWidth / dates.size();
            for (int i = 0; i < dates.size(); i++) {
                int x = padding + (barWidth * i);
                int barHeight = volumes.get(i) * chartHeight / maxVolume;
                int y = padding + chartHeight - barHeight;
                String color = volumes.get(i) >= dailyTargetMl ? "#67C23A" : "#409EFF";
                svg.append("<rect x=\"").append(x + 2).append("\" y=\"").append(y)
                    .append("\" width=\"").append(barWidth - 4).append("\" height=\"").append(barHeight)
                    .append("\" fill=\"").append(color).append("\" opacity=\"0.8\"/>\n");
            }
        }

        // X轴标签
        int labelInterval = Math.max(1, dates.size() / 10);
        int barWidth = dates.size() > 0 ? chartWidth / dates.size() : 0;
        for (int i = 0; i < dates.size(); i += labelInterval) {
            int x = padding + (barWidth * i) + barWidth / 2;
            String dateLabel = dates.get(i).substring(5); // 只显示月-日
            svg.append("<text x=\"").append(x).append("\" y=\"").append(height - padding + 20)
                .append("\" font-size=\"10\" fill=\"#666\" text-anchor=\"middle\">").append(dateLabel).append("</text>\n");
        }

        svg.append("</svg>");
        String svgString = svg.toString();
        // 将SVG转换为Base64编码的data URI，以便在邮件中显示
        try {
            String base64Svg = Base64.getEncoder().encodeToString(svgString.getBytes(StandardCharsets.UTF_8));
            return "<div style=\"text-align: center; margin: 20px 0; padding: 10px;\">\n" +
                   "<img src=\"data:image/svg+xml;base64," + base64Svg + "\" alt=\"饮水趋势图\" " +
                   "style=\"max-width: 100%; width: 700px; height: auto; display: block; margin: 0 auto; border: 1px solid #e0e0e0;\"/>\n" +
                   "</div>";
        } catch (Exception e) {
            log.error("编码SVG失败", e);
            return "<p style=\"text-align: center; color: #999;\">图表生成失败</p>";
        }
    }

    /**
     * 从Map中获取整数值
     */
    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * 从Map中获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key, BigDecimal defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return defaultValue;
    }

    /**
     * 检查任务是否启用
     */
    private boolean isTaskEnabled() {
        return isTaskEnabled("healthDailyReport.enabled");
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
     * @param configKey 定时任务配置键（如：healthDailyReport.enabled）
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

