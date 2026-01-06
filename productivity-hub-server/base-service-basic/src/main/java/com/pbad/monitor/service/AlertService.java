package com.pbad.monitor.service;

import com.pbad.monitor.domain.dto.AlertRuleDTO;
import com.pbad.monitor.domain.vo.AlertVO;

import java.util.List;

/**
 * 告警服务接口
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
public interface AlertService {

    /**
     * 检查告警规则并触发告警
     */
    void checkAndTriggerAlerts();

    /**
     * 获取告警列表
     *
     * @param handled 是否已处理（null表示全部）
     * @return 告警列表
     */
    List<AlertVO> getAlerts(Boolean handled);

    /**
     * 处理告警
     *
     * @param alertId 告警ID
     */
    void handleAlert(String alertId);

    /**
     * 添加告警规则
     *
     * @param rule 告警规则
     */
    void addAlertRule(AlertRuleDTO rule);

    /**
     * 获取所有告警规则
     *
     * @return 告警规则列表
     */
    List<AlertRuleDTO> getAlertRules();

    /**
     * 删除告警规则
     *
     * @param ruleId 规则ID
     */
    void deleteAlertRule(String ruleId);
}

