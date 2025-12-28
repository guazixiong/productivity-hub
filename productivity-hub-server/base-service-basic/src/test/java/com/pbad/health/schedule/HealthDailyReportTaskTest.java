package com.pbad.health.schedule;

import com.pbad.config.service.ConfigService;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.mapper.*;
import com.pbad.messages.domain.dto.MessageSendDTO;
import com.pbad.messages.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户每日健康统计邮件推送定时任务单元测试.
 * 关联测试用例：TC-EMAIL-001至TC-EMAIL-007
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthDailyReportTaskTest {

    @Mock
    private HealthUserBodyInfoMapper userBodyInfoMapper;

    @Mock
    private HealthExerciseRecordMapper exerciseRecordMapper;

    @Mock
    private HealthWaterIntakeMapper waterIntakeMapper;

    @Mock
    private HealthWaterTargetMapper waterTargetMapper;

    @Mock
    private HealthWeightRecordMapper weightRecordMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private HealthDailyReportTask healthDailyReportTask;

    private static final String TEST_USER_ID = "pd_user_001";
    private static final String TEST_EMAIL = "test@example.com";

    /**
     * TC-EMAIL-001: 用户健康统计邮件-任务开关关闭
     */
    @Test
    void testSendDailyHealthReport_TaskDisabled() {
        // Mock任务开关为关闭
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("false");

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：不查询用户，不发送邮件
        verify(userBodyInfoMapper, never()).selectPDUsers();
        verify(messageService, never()).sendMessage(any(), any());
    }

    /**
     * TC-EMAIL-002: 用户健康统计邮件-正常执行
     */
    @Test
    void testSendDailyHealthReport_Success() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户
        HealthUserBodyInfoPO user = new HealthUserBodyInfoPO();
        user.setUserId(TEST_USER_ID);
        user.setResendEmail(TEST_EMAIL);
        user.setTargetWeightKg(new BigDecimal("68.0"));
        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // Mock运动统计
        Map<String, Object> exerciseStats = new HashMap<>();
        exerciseStats.put("totalCount", 2);
        exerciseStats.put("totalDuration", 60);
        exerciseStats.put("totalCalories", 500);
        exerciseStats.put("totalDistance", new BigDecimal("5.0"));
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayEnd = Date.from(yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        when(exerciseRecordMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(exerciseStats);

        // Mock饮水统计
        Map<String, Object> waterStats = new HashMap<>();
        waterStats.put("totalCount", 5);
        waterStats.put("totalVolume", 2000);
        when(waterIntakeMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(waterStats);
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, yesterdayStart)).thenReturn(2000);

        // Mock体重趋势
        List<Map<String, Object>> weightTrend = new ArrayList<>();
        Map<String, Object> weightData = new HashMap<>();
        weightData.put("date", yesterday.toString());
        weightData.put("value", new BigDecimal("70.5"));
        weightTrend.add(weightData);
        when(weightRecordMapper.queryTrendData(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(weightTrend);

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：发送了邮件
        ArgumentCaptor<MessageSendDTO> messageCaptor = ArgumentCaptor.forClass(MessageSendDTO.class);
        verify(messageService, times(1)).sendMessage(messageCaptor.capture(), eq(TEST_USER_ID));

        MessageSendDTO sentMessage = messageCaptor.getValue();
        assertEquals("resend", sentMessage.getChannel());
        assertNotNull(sentMessage.getData());
    }

    /**
     * TC-EMAIL-003: 用户健康统计邮件-用户未启用推送
     */
    @Test
    void testSendDailyHealthReport_UserNotEnabled() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户（但未启用推送，selectPDUsers应该只返回启用的用户）
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(Collections.emptyList());

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：不发送邮件
        verify(messageService, never()).sendMessage(any(), any());
    }

    /**
     * TC-EMAIL-004: 用户健康统计邮件-用户未配置邮箱
     */
    @Test
    void testSendDailyHealthReport_NoEmail() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户（未配置邮箱）
        HealthUserBodyInfoPO user = new HealthUserBodyInfoPO();
        user.setUserId(TEST_USER_ID);
        user.setResendEmail(null); // 未配置邮箱
        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：不发送邮件
        verify(messageService, never()).sendMessage(any(), any());
    }

    /**
     * TC-EMAIL-005: 用户健康统计邮件-非用户
     * 注意：selectPDUsers方法应该只返回用户，所以这个测试主要验证方法逻辑
     */
    @Test
    void testSendDailyHealthReport_NonPDUser() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock selectPDUsers返回空列表（非用户不会被查询到）
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(Collections.emptyList());

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：不发送邮件
        verify(messageService, never()).sendMessage(any(), any());
    }

    /**
     * TC-EMAIL-006: 用户健康统计邮件-昨日无数据
     */
    @Test
    void testSendDailyHealthReport_NoData() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户
        HealthUserBodyInfoPO user = new HealthUserBodyInfoPO();
        user.setUserId(TEST_USER_ID);
        user.setResendEmail(TEST_EMAIL);
        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // Mock无数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayEnd = Date.from(yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        when(exerciseRecordMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(null);
        when(waterIntakeMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(null);
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, yesterdayStart)).thenReturn(0);
        when(weightRecordMapper.queryTrendData(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(Collections.emptyList());

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：仍然发送邮件（即使无数据也发送，显示0值）
        ArgumentCaptor<MessageSendDTO> messageCaptor = ArgumentCaptor.forClass(MessageSendDTO.class);
        verify(messageService, times(1)).sendMessage(messageCaptor.capture(), eq(TEST_USER_ID));

        MessageSendDTO sentMessage = messageCaptor.getValue();
        assertEquals("resend", sentMessage.getChannel());
    }

    /**
     * TC-EMAIL-007: 用户健康统计邮件-Resend服务调用失败
     */
    @Test
    void testSendDailyHealthReport_ResendFailure() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户
        HealthUserBodyInfoPO user = new HealthUserBodyInfoPO();
        user.setUserId(TEST_USER_ID);
        user.setResendEmail(TEST_EMAIL);
        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // Mock运动统计
        Map<String, Object> exerciseStats = new HashMap<>();
        exerciseStats.put("totalCount", 1);
        exerciseStats.put("totalDuration", 30);
        exerciseStats.put("totalCalories", 200);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayEnd = Date.from(yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        when(exerciseRecordMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(exerciseStats);

        // Mock饮水统计
        Map<String, Object> waterStats = new HashMap<>();
        when(waterIntakeMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(waterStats);
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, yesterdayStart)).thenReturn(1000);

        // Mock体重趋势
        when(weightRecordMapper.queryTrendData(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(Collections.emptyList());

        // Mock Resend服务调用失败
        doThrow(new RuntimeException("Resend服务不可用"))
                .when(messageService).sendMessage(any(MessageSendDTO.class), anyString());

        // 执行任务（不应该抛出异常）
        assertDoesNotThrow(() -> healthDailyReportTask.sendDailyHealthReport());

        // 验证：尝试发送了邮件
        verify(messageService, times(1)).sendMessage(any(MessageSendDTO.class), eq(TEST_USER_ID));
    }

    /**
     * 测试：多个用户处理
     */
    @Test
    void testSendDailyHealthReport_MultipleUsers() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock多个用户
        HealthUserBodyInfoPO user1 = new HealthUserBodyInfoPO();
        user1.setUserId("pd_user_001");
        user1.setResendEmail("user1@example.com");

        HealthUserBodyInfoPO user2 = new HealthUserBodyInfoPO();
        user2.setUserId("pd_user_002");
        user2.setResendEmail("user2@example.com");

        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user1, user2);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // Mock每个用户的数据
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayEnd = Date.from(yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        for (HealthUserBodyInfoPO user : pdUsers) {
            Map<String, Object> exerciseStats = new HashMap<>();
            exerciseStats.put("totalCount", 1);
            when(exerciseRecordMapper.statisticsByDateRange(user.getUserId(), yesterdayStart, yesterdayEnd))
                    .thenReturn(exerciseStats);
            when(waterIntakeMapper.statisticsByDateRange(user.getUserId(), yesterdayStart, yesterdayEnd))
                    .thenReturn(new HashMap<>());
            when(waterIntakeMapper.sumVolumeByDate(user.getUserId(), yesterdayStart)).thenReturn(1000);
            when(weightRecordMapper.queryTrendData(user.getUserId(), yesterdayStart, yesterdayEnd))
                    .thenReturn(Collections.emptyList());
        }

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：为每个用户都发送了邮件
        verify(messageService, times(2)).sendMessage(any(MessageSendDTO.class), anyString());
    }

    /**
     * 测试：邮件内容包含所有统计信息
     */
    @Test
    void testSendDailyHealthReport_EmailContent() {
        // Mock任务开关为开启
        when(configService.getTemplateConfigValue("schedule", "healthDailyReport.enabled"))
                .thenReturn("true");

        // Mock 用户
        HealthUserBodyInfoPO user = new HealthUserBodyInfoPO();
        user.setUserId(TEST_USER_ID);
        user.setResendEmail(TEST_EMAIL);
        user.setTargetWeightKg(new BigDecimal("68.0"));
        List<HealthUserBodyInfoPO> pdUsers = Arrays.asList(user);
        when(userBodyInfoMapper.selectPDUsers()).thenReturn(pdUsers);

        // Mock完整数据
        Map<String, Object> exerciseStats = new HashMap<>();
        exerciseStats.put("totalCount", 3);
        exerciseStats.put("totalDuration", 90);
        exerciseStats.put("totalCalories", 600);
        exerciseStats.put("totalDistance", new BigDecimal("8.5"));

        Map<String, Object> waterStats = new HashMap<>();
        waterStats.put("totalCount", 6);
        waterStats.put("totalVolume", 2500);

        List<Map<String, Object>> weightTrend = new ArrayList<>();
        Map<String, Object> weightData = new HashMap<>();
        weightData.put("date", LocalDate.now().minusDays(1).toString());
        weightData.put("value", new BigDecimal("70.5"));
        weightTrend.add(weightData);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayEnd = Date.from(yesterday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        when(exerciseRecordMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(exerciseStats);
        when(waterIntakeMapper.statisticsByDateRange(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(waterStats);
        when(waterIntakeMapper.sumVolumeByDate(TEST_USER_ID, yesterdayStart)).thenReturn(2500);
        when(weightRecordMapper.queryTrendData(TEST_USER_ID, yesterdayStart, yesterdayEnd))
                .thenReturn(weightTrend);

        // 执行任务
        healthDailyReportTask.sendDailyHealthReport();

        // 验证：发送了邮件，并验证邮件内容
        ArgumentCaptor<MessageSendDTO> messageCaptor = ArgumentCaptor.forClass(MessageSendDTO.class);
        verify(messageService, times(1)).sendMessage(messageCaptor.capture(), eq(TEST_USER_ID));

        MessageSendDTO sentMessage = messageCaptor.getValue();
        assertEquals("resend", sentMessage.getChannel());
        assertNotNull(sentMessage.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) sentMessage.getData();
        assertEquals(TEST_EMAIL, data.get("to"));
        assertNotNull(data.get("title"));
        assertNotNull(data.get("html"));
        String html = (String) data.get("html");
        assertTrue(html.contains("健康统计日报"));
        assertTrue(html.contains("运动统计"));
        assertTrue(html.contains("饮水统计"));
        assertTrue(html.contains("体重统计"));
    }
}

