package com.pbad.health.schedule;

import com.pbad.config.service.ConfigService;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import com.pbad.thirdparty.api.MessageChannelApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 钉钉饮水提醒定时任务单元测试.
 * 关联测试用例：TC-TASK-001至TC-TASK-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class WaterReminderTaskTest {

    @Mock
    private HealthWaterTargetMapper waterTargetMapper;

    @Mock
    private HealthWaterIntakeMapper waterIntakeMapper;

    @Mock
    private MessageChannelApi dingtalkChannelApi;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private WaterReminderTask waterReminderTask;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_WEBHOOK_URL = "https://oapi.dingtalk.com/robot/send?access_token=test_token";

    /**
     * TC-TASK-001: 钉钉饮水提醒-任务开关关闭
     */
    @Test
    void testSendWaterReminder_TaskDisabled() {
        // Mock任务开关为关闭
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("false");

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：不查询用户，不发送消息
        verify(waterTargetMapper, never()).selectUserIdsWithReminderEnabled();
        verify(dingtalkChannelApi, never()).sendMessage(any(), any());
    }

    /**
     * TC-TASK-002: 钉钉饮水提醒-正常执行
     */
    @Test
    void testSendWaterReminder_Success() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock用户配置
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(TEST_WEBHOOK_URL);

        // Mock饮水目标
        HealthWaterTargetPO target = new HealthWaterTargetPO();
        target.setUserId(TEST_USER_ID);
        target.setDailyTargetMl(2000);
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(target);

        // Mock今日已饮水量
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, todayStart)).thenReturn(800);

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：发送了消息
        verify(dingtalkChannelApi, times(1)).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * TC-TASK-003: 钉钉饮水提醒-用户已达标
     */
    @Test
    void testSendWaterReminder_UserAchieved() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock用户配置
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(TEST_WEBHOOK_URL);

        // Mock饮水目标
        HealthWaterTargetPO target = new HealthWaterTargetPO();
        target.setUserId(TEST_USER_ID);
        target.setDailyTargetMl(2000);
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(target);

        // Mock今日已饮水量（已达标）
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, todayStart)).thenReturn(2000);

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：发送了消息（即使已达标也可能发送鼓励消息）
        verify(dingtalkChannelApi, times(1)).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * TC-TASK-004: 钉钉饮水提醒-用户未配置Webhook
     */
    @Test
    void testSendWaterReminder_NoWebhook() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock用户未配置Webhook
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(null);
        when(configService.getConfigValue("dingtalk", "dingtalk.webhook", TEST_USER_ID))
                .thenReturn(null);

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：不发送消息
        verify(dingtalkChannelApi, never()).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * TC-TASK-005: 钉钉饮水提醒-Webhook调用失败
     */
    @Test
    void testSendWaterReminder_WebhookFailure() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock用户配置
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(TEST_WEBHOOK_URL);

        // Mock饮水目标
        HealthWaterTargetPO target = new HealthWaterTargetPO();
        target.setUserId(TEST_USER_ID);
        target.setDailyTargetMl(2000);
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(target);

        // Mock今日已饮水量
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, todayStart)).thenReturn(800);

        // Mock Webhook调用失败
        doThrow(new RuntimeException("Webhook服务不可用"))
                .when(dingtalkChannelApi).sendMessage(any(Map.class), any(Map.class));

        // 执行任务（不应该抛出异常）
        assertDoesNotThrow(() -> waterReminderTask.sendWaterReminder());

        // 验证：尝试发送了消息
        verify(dingtalkChannelApi, times(1)).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * TC-TASK-006: 钉钉饮水提醒-多个用户处理
     */
    @Test
    void testSendWaterReminder_MultipleUsers() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock多个用户
        List<String> userIds = Arrays.asList("user_001", "user_002", "user_003");
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock每个用户的配置
        for (String userId : userIds) {
            String webhookKey = "waterReminder.dingtalkWebhook." + userId;
            when(configService.getConfigValue("health", webhookKey, userId))
                    .thenReturn(TEST_WEBHOOK_URL);

            HealthWaterTargetPO target = new HealthWaterTargetPO();
            target.setUserId(userId);
            target.setDailyTargetMl(2000);
            when(waterTargetMapper.selectByUserId(userId)).thenReturn(target);

            LocalDate today = LocalDate.now();
            Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            when(waterIntakeMapper.sumVolumeByDate(userId, todayStart)).thenReturn(800);
        }

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：为每个用户都发送了消息
        verify(dingtalkChannelApi, times(3)).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * 测试：无用户时跳过执行
     */
    @Test
    void testSendWaterReminder_NoUsers() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock无用户
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(Collections.emptyList());

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：不发送消息
        verify(dingtalkChannelApi, never()).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * 测试：使用默认目标饮水量
     */
    @Test
    void testSendWaterReminder_DefaultTarget() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock用户配置
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(TEST_WEBHOOK_URL);

        // Mock用户无饮水目标（使用默认值2000ml）
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);

        // Mock今日已饮水量
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, todayStart)).thenReturn(500);

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：发送了消息（使用默认目标2000ml）
        verify(dingtalkChannelApi, times(1)).sendMessage(any(Map.class), any(Map.class));
    }

    /**
     * 测试：从dingtalk模块读取默认Webhook
     */
    @Test
    void testSendWaterReminder_DefaultWebhook() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "waterReminder.enabled"))
                .thenReturn("true");

        // Mock用户列表
        List<String> userIds = Arrays.asList(TEST_USER_ID);
        when(waterTargetMapper.selectUserIdsWithReminderEnabled()).thenReturn(userIds);

        // Mock health模块无配置，从dingtalk模块读取
        String webhookKey = "waterReminder.dingtalkWebhook." + TEST_USER_ID;
        when(configService.getConfigValue("health", webhookKey, TEST_USER_ID))
                .thenReturn(null);
        when(configService.getConfigValue("dingtalk", "dingtalk.webhook", TEST_USER_ID))
                .thenReturn(TEST_WEBHOOK_URL);

        // Mock饮水目标
        HealthWaterTargetPO target = new HealthWaterTargetPO();
        target.setUserId(TEST_USER_ID);
        target.setDailyTargetMl(2000);
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(target);

        // Mock今日已饮水量
        LocalDate today = LocalDate.now();
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, todayStart)).thenReturn(800);

        // 执行任务
        waterReminderTask.sendWaterReminder();

        // 验证：发送了消息
        verify(dingtalkChannelApi, times(1)).sendMessage(any(Map.class), any(Map.class));
    }
}

