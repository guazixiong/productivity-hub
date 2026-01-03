package com.pbad.monitor.service.impl;

import com.pbad.monitor.domain.dto.AlertRuleDTO;
import com.pbad.monitor.domain.vo.AlertVO;
import com.pbad.monitor.domain.vo.ApplicationMetricsVO;
import com.pbad.monitor.domain.vo.SystemMetricsVO;
import com.pbad.monitor.service.AlertService;
import com.pbad.monitor.service.MonitorService;
import com.pbad.notifications.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 告警服务实现
 * TASK-BACKEND-10: 监控与告警实现
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    private final MonitorService monitorService;
    private final NotificationService notificationService;

    // 告警规则存储（生产环境应使用数据库）
    private final Map<String, AlertRuleDTO> alertRules = new ConcurrentHashMap<>();

    // 告警记录存储（生产环境应使用数据库）
    private final Map<String, AlertVO> alerts = new ConcurrentHashMap<>();

    public AlertServiceImpl(MonitorService monitorService, NotificationService notificationService) {
        this.monitorService = monitorService;
        this.notificationService = notificationService;
        initDefaultRules();
    }

    /**
     * 初始化默认告警规则
     */
    private void initDefaultRules() {
        // CPU使用率告警
        AlertRuleDTO cpuRule = new AlertRuleDTO();
        cpuRule.setId("rule_cpu_usage");
        cpuRule.setMetricName("cpu_usage");
        cpuRule.setThreshold(80.0);
        cpuRule.setOperator(">");
        cpuRule.setLevel("WARN");
        cpuRule.setEnabled(true);
        cpuRule.setMessageTemplate("CPU使用率过高: {value}%");
        alertRules.put(cpuRule.getId(), cpuRule);

        // 内存使用率告警
        AlertRuleDTO memoryRule = new AlertRuleDTO();
        memoryRule.setId("rule_memory_usage");
        memoryRule.setMetricName("memory_usage");
        memoryRule.setThreshold(85.0);
        memoryRule.setOperator(">");
        memoryRule.setLevel("WARN");
        memoryRule.setEnabled(true);
        memoryRule.setMessageTemplate("内存使用率过高: {value}%");
        alertRules.put(memoryRule.getId(), memoryRule);

        // 错误率告警
        AlertRuleDTO errorRateRule = new AlertRuleDTO();
        errorRateRule.setId("rule_error_rate");
        errorRateRule.setMetricName("error_rate");
        errorRateRule.setThreshold(5.0);
        errorRateRule.setOperator(">");
        errorRateRule.setLevel("ERROR");
        errorRateRule.setEnabled(true);
        errorRateRule.setMessageTemplate("错误率过高: {value}%");
        alertRules.put(errorRateRule.getId(), errorRateRule);
    }

    @Override
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkAndTriggerAlerts() {
        if (alertRules.isEmpty()) {
            return;
        }

        try {
            // 获取系统指标
            SystemMetricsVO systemMetrics = monitorService.getSystemMetrics();
            ApplicationMetricsVO applicationMetrics = monitorService.getApplicationMetrics();

            // 检查所有启用的规则
            for (AlertRuleDTO rule : alertRules.values()) {
                if (!rule.getEnabled()) {
                    continue;
                }

                Double metricValue = getMetricValue(rule.getMetricName(), systemMetrics, applicationMetrics);
                if (metricValue == null) {
                    continue;
                }

                // 检查是否触发告警
                if (checkThreshold(metricValue, rule.getThreshold(), rule.getOperator())) {
                    triggerAlert(rule, metricValue);
                }
            }
        } catch (Exception e) {
            log.error("检查告警规则失败", e);
        }
    }

    @Override
    public List<AlertVO> getAlerts(Boolean handled) {
        return alerts.values().stream()
                .filter(alert -> handled == null || alert.getHandled().equals(handled))
                .sorted(Comparator.comparing(AlertVO::getAlertTime).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void handleAlert(String alertId) {
        AlertVO alert = alerts.get(alertId);
        if (alert != null) {
            alert.setHandled(true);
            alert.setHandledTime(LocalDateTime.now());
        }
    }

    @Override
    public void addAlertRule(AlertRuleDTO rule) {
        if (rule.getId() == null) {
            rule.setId("rule_" + System.currentTimeMillis());
        }
        alertRules.put(rule.getId(), rule);
    }

    @Override
    public List<AlertRuleDTO> getAlertRules() {
        return new ArrayList<>(alertRules.values());
    }

    @Override
    public void deleteAlertRule(String ruleId) {
        alertRules.remove(ruleId);
    }

    /**
     * 获取指标值
     */
    private Double getMetricValue(String metricName, SystemMetricsVO systemMetrics, ApplicationMetricsVO applicationMetrics) {
        switch (metricName) {
            case "cpu_usage":
                return systemMetrics.getCpuUsage();
            case "memory_usage":
                return systemMetrics.getMemoryUsage();
            case "error_rate":
                return applicationMetrics.getErrorRate();
            case "avg_response_time":
                return applicationMetrics.getAvgResponseTime();
            default:
                // 从指标列表中查找
                if (systemMetrics.getMetrics() != null) {
                    return systemMetrics.getMetrics().stream()
                            .filter(m -> m.getName().equals(metricName))
                            .map(m -> m.getValue())
                            .findFirst()
                            .orElse(null);
                }
                if (applicationMetrics.getMetrics() != null) {
                    return applicationMetrics.getMetrics().stream()
                            .filter(m -> m.getName().equals(metricName))
                            .map(m -> m.getValue())
                            .findFirst()
                            .orElse(null);
                }
                return null;
        }
    }

    /**
     * 检查阈值
     */
    private boolean checkThreshold(Double value, Double threshold, String operator) {
        if (value == null || threshold == null) {
            return false;
        }
        switch (operator) {
            case ">":
                return value > threshold;
            case ">=":
                return value >= threshold;
            case "<":
                return value < threshold;
            case "<=":
                return value <= threshold;
            case "==":
                return Math.abs(value - threshold) < 0.01;
            default:
                return false;
        }
    }

    /**
     * 触发告警
     */
    private void triggerAlert(AlertRuleDTO rule, Double metricValue) {
        String alertId = "alert_" + System.currentTimeMillis() + "_" + rule.getId();
        
        // 检查是否已存在相同告警（避免重复告警）
        boolean exists = alerts.values().stream()
                .anyMatch(alert -> alert.getMetricName().equals(rule.getMetricName())
                        && !alert.getHandled()
                        && alert.getAlertTime().isAfter(LocalDateTime.now().minusMinutes(5)));

        if (exists) {
            return; // 5分钟内已存在相同告警，不重复触发
        }

        AlertVO alert = new AlertVO();
        alert.setId(alertId);
        alert.setMetricName(rule.getMetricName());
        alert.setMetricValue(metricValue);
        alert.setLevel(rule.getLevel());
        alert.setMessage(rule.getMessageTemplate().replace("{value}", String.format("%.2f", metricValue)));
        alert.setAlertTime(LocalDateTime.now());
        alert.setHandled(false);

        alerts.put(alertId, alert);

        // 发送通知（简化实现）
        try {
            String userId = "admin"; // 实际应该从配置或用户表获取
            notificationService.publish(com.pbad.notifications.domain.dto.NotificationPublishDTO.builder()
                    .userId(userId)
                    .title("系统告警: " + rule.getLevel())
                    .content(alert.getMessage())
                    .path("/monitor/alerts")
                    .build());
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
        }

        log.warn("触发告警: {}", alert.getMessage());
    }
}

