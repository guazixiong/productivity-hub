package com.pbad.monitor.controller;

import com.pbad.monitor.domain.dto.AlertRuleDTO;
import com.pbad.monitor.domain.vo.AlertVO;
import com.pbad.monitor.domain.vo.ApplicationMetricsVO;
import com.pbad.monitor.domain.vo.SystemMetricsVO;
import com.pbad.monitor.service.AlertService;
import com.pbad.monitor.service.MonitorService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控控制器
 * TASK-BACKEND-10: 监控与告警实现
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;
    private final AlertService alertService;

    /**
     * 获取系统监控指标
     */
    @GetMapping("/system")
    public ApiResponse<SystemMetricsVO> getSystemMetrics() {
        SystemMetricsVO metrics = monitorService.getSystemMetrics();
        return ApiResponse.ok(metrics);
    }

    /**
     * 获取应用监控指标
     */
    @GetMapping("/application")
    public ApiResponse<ApplicationMetricsVO> getApplicationMetrics() {
        ApplicationMetricsVO metrics = monitorService.getApplicationMetrics();
        return ApiResponse.ok(metrics);
    }

    /**
     * 获取告警列表
     */
    @GetMapping("/alerts")
    public ApiResponse<List<AlertVO>> getAlerts(@RequestParam(required = false) Boolean handled) {
        List<AlertVO> alerts = alertService.getAlerts(handled);
        return ApiResponse.ok(alerts);
    }

    /**
     * 处理告警
     */
    @PostMapping("/alerts/{alertId}/handle")
    public ApiResponse<String> handleAlert(@PathVariable String alertId) {
        alertService.handleAlert(alertId);
        return ApiResponse.ok("告警已处理");
    }

    /**
     * 获取告警规则列表
     */
    @GetMapping("/alert-rules")
    public ApiResponse<List<AlertRuleDTO>> getAlertRules() {
        List<AlertRuleDTO> rules = alertService.getAlertRules();
        return ApiResponse.ok(rules);
    }

    /**
     * 添加告警规则
     */
    @PostMapping("/alert-rules")
    public ApiResponse<String> addAlertRule(@RequestBody AlertRuleDTO rule) {
        alertService.addAlertRule(rule);
        return ApiResponse.ok("告警规则已添加");
    }

    /**
     * 删除告警规则
     */
    @DeleteMapping("/alert-rules/{ruleId}")
    public ApiResponse<String> deleteAlertRule(@PathVariable String ruleId) {
        alertService.deleteAlertRule(ruleId);
        return ApiResponse.ok("告警规则已删除");
    }
}

