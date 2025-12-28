package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.WaterTargetCreateOrUpdateDTO;
import com.pbad.health.domain.po.HealthWaterIntakePO;
import com.pbad.health.domain.po.HealthWaterTargetPO;
import com.pbad.health.domain.vo.WaterTargetProgressVO;
import com.pbad.health.domain.vo.WaterTargetVO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.mapper.HealthWaterTargetMapper;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 饮水目标服务实现类单元测试.
 * 关联测试用例：TC-TARGET-001、TC-WATER-006至TC-WATER-008
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthWaterTargetServiceImplTest {

    @Mock
    private HealthWaterTargetMapper waterTargetMapper;

    @Mock
    private HealthWaterIntakeMapper waterIntakeMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthWaterTargetServiceImpl waterTargetService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_TARGET_ID = "water_target_001";

    @BeforeEach
    void setUp() {
        when(idGeneratorApi.generateId()).thenReturn(TEST_TARGET_ID);
    }

    // ==================== 创建/更新饮水目标测试 ====================

    /**
     * TC-TARGET-001: 创建饮水目标-成功
     */
    @Test
    void testCreateOrUpdate_CreateSuccess() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();
        dto.setDailyTargetMl(2500);
        dto.setReminderIntervals(Arrays.asList("09:00", "12:00", "15:00", "18:00"));

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(waterTargetMapper.insert(any(HealthWaterTargetPO.class))).thenReturn(1);

        WaterTargetVO result = waterTargetService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_TARGET_ID, result.getId());
        assertEquals(2500, result.getDailyTargetMl());
        assertTrue(result.getReminderEnabled());
        assertNotNull(result.getReminderIntervals());
        assertEquals(4, result.getReminderIntervals().size());

        verify(waterTargetMapper, times(1)).selectByUserId(TEST_USER_ID);
        verify(waterTargetMapper, times(1)).insert(any(HealthWaterTargetPO.class));
        verify(idGeneratorApi, times(1)).generateId();
    }

    /**
     * TC-TARGET-001: 更新饮水目标-成功
     */
    @Test
    void testCreateOrUpdate_UpdateSuccess() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();
        dto.setDailyTargetMl(3000);

        HealthWaterTargetPO existingPo = createTestPO();
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(existingPo);
        when(waterTargetMapper.update(any(HealthWaterTargetPO.class))).thenReturn(1);

        WaterTargetVO result = waterTargetService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(3000, result.getDailyTargetMl());

        verify(waterTargetMapper, times(1)).selectByUserId(TEST_USER_ID);
        verify(waterTargetMapper, times(1)).update(any(HealthWaterTargetPO.class));
        verify(waterTargetMapper, never()).insert(any(HealthWaterTargetPO.class));
    }

    /**
     * TC-TARGET-001-边界: 创建饮水目标-使用默认值
     */
    @Test
    void testCreateOrUpdate_WithDefaultValues() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(waterTargetMapper.insert(any(HealthWaterTargetPO.class))).thenReturn(1);

        WaterTargetVO result = waterTargetService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(2000, result.getDailyTargetMl()); // 默认值
        assertFalse(result.getReminderEnabled());
    }

    /**
     * TC-TARGET-001-异常: 创建/更新饮水目标-参数为空
     */
    @Test
    void testCreateOrUpdate_NullDTO() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterTargetService.createOrUpdate(null, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("请求参数不能为空"));
    }

    /**
     * TC-TARGET-001-异常: 创建/更新饮水目标-每日目标饮水量过小
     */
    @Test
    void testCreateOrUpdate_DailyTargetMlTooSmall() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();
        dto.setDailyTargetMl(499); // 小于500

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterTargetService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("每日目标饮水量必须在500-10000毫升之间"));
    }

    /**
     * TC-TARGET-001-异常: 创建/更新饮水目标-每日目标饮水量过大
     */
    @Test
    void testCreateOrUpdate_DailyTargetMlTooLarge() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();
        dto.setDailyTargetMl(10001); // 大于10000

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterTargetService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("每日目标饮水量必须在500-10000毫升之间"));
    }

    /**
     * TC-TARGET-001-异常: 更新饮水目标-没有需要更新的字段
     */
    @Test
    void testCreateOrUpdate_NoUpdateFields() {
        WaterTargetCreateOrUpdateDTO dto = new WaterTargetCreateOrUpdateDTO();

        HealthWaterTargetPO existingPo = createTestPO();
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(existingPo);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterTargetService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("没有需要更新的字段"));
    }

    // ==================== 查询饮水目标测试 ====================

    /**
     * TC-WATER-006: 查询饮水目标-成功（存在配置）
     */
    @Test
    void testGetByUserId_Success() {
        HealthWaterTargetPO po = createTestPO();
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(po);

        WaterTargetVO result = waterTargetService.getByUserId(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_TARGET_ID, result.getId());
        assertEquals(2500, result.getDailyTargetMl());
        verify(waterTargetMapper, times(1)).selectByUserId(TEST_USER_ID);
    }

    /**
     * TC-WATER-006: 查询饮水目标-返回默认值（不存在配置）
     */
    @Test
    void testGetByUserId_ReturnDefault() {
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);

        WaterTargetVO result = waterTargetService.getByUserId(TEST_USER_ID);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(2000, result.getDailyTargetMl()); // 默认值
        assertFalse(result.getReminderEnabled());
        assertNull(result.getReminderIntervals());
    }

    // ==================== 查询饮水进度测试 ====================

    /**
     * TC-WATER-007: 查询饮水进度-成功（已达标）
     */
    @Test
    void testGetProgress_Achieved() {
        HealthWaterTargetPO targetPo = createTestPO();
        targetPo.setDailyTargetMl(2000);

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(targetPo);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(2500);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, null);

        assertNotNull(result);
        assertEquals(2000, result.getTargetMl());
        assertEquals(2500, result.getConsumedMl());
        assertEquals(0, result.getRemainingMl());
        assertTrue(result.getIsAchieved());
        assertTrue(result.getProgressPercentage() >= 100.0);
    }

    /**
     * TC-WATER-007: 查询饮水进度-成功（未达标）
     */
    @Test
    void testGetProgress_NotAchieved() {
        HealthWaterTargetPO targetPo = createTestPO();
        targetPo.setDailyTargetMl(2000);

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(targetPo);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(1200);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, null);

        assertNotNull(result);
        assertEquals(2000, result.getTargetMl());
        assertEquals(1200, result.getConsumedMl());
        assertEquals(800, result.getRemainingMl());
        assertFalse(result.getIsAchieved());
        assertEquals(60.0, result.getProgressPercentage());
    }

    /**
     * TC-WATER-007: 查询饮水进度-使用默认目标值
     */
    @Test
    void testGetProgress_WithDefaultTarget() {
        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(1500);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, null);

        assertNotNull(result);
        assertEquals(2000, result.getTargetMl()); // 默认值
        assertEquals(1500, result.getConsumedMl());
        assertEquals(500, result.getRemainingMl());
    }

    /**
     * TC-WATER-007: 查询饮水进度-无饮水记录
     */
    @Test
    void testGetProgress_NoIntakeRecords() {
        HealthWaterTargetPO targetPo = createTestPO();
        targetPo.setDailyTargetMl(2000);

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(targetPo);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(null);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, null);

        assertNotNull(result);
        assertEquals(2000, result.getTargetMl());
        assertEquals(0, result.getConsumedMl());
        assertEquals(2000, result.getRemainingMl());
        assertFalse(result.getIsAchieved());
        assertEquals(0.0, result.getProgressPercentage());
    }

    /**
     * TC-WATER-007: 查询饮水进度-指定日期
     */
    @Test
    void testGetProgress_WithSpecificDate() {
        HealthWaterTargetPO targetPo = createTestPO();
        targetPo.setDailyTargetMl(2000);

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(targetPo);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(1800);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, "2025-01-15");

        assertNotNull(result);
        assertEquals("2025-01-15", result.getQueryDate());
        assertEquals(1800, result.getConsumedMl());
    }

    /**
     * TC-WATER-007-异常: 查询饮水进度-日期格式错误
     */
    @Test
    void testGetProgress_InvalidDateFormat() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterTargetService.getProgress(TEST_USER_ID, "2025/01/15");
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("查询日期格式错误"));
    }

    /**
     * TC-WATER-008: 查询饮水进度-进度超过100%
     */
    @Test
    void testGetProgress_Exceeds100Percent() {
        HealthWaterTargetPO targetPo = createTestPO();
        targetPo.setDailyTargetMl(2000);

        when(waterTargetMapper.selectByUserId(TEST_USER_ID)).thenReturn(targetPo);
        when(waterIntakeMapper.sumVolumeByDate(eq(TEST_USER_ID), any(Date.class))).thenReturn(3000);

        WaterTargetProgressVO result = waterTargetService.getProgress(TEST_USER_ID, null);

        assertNotNull(result);
        assertEquals(100.0, result.getProgressPercentage()); // 应该被限制为100%
        assertTrue(result.getIsAchieved());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的PO对象
     */
    private HealthWaterTargetPO createTestPO() {
        HealthWaterTargetPO po = new HealthWaterTargetPO();
        po.setId(TEST_TARGET_ID);
        po.setUserId(TEST_USER_ID);
        po.setDailyTargetMl(2500);
        po.setReminderEnabled(1);
        po.setReminderIntervals("[\"09:00\",\"12:00\",\"15:00\",\"18:00\"]");
        po.setCreatedAt(new Date());
        po.setUpdatedAt(new Date());
        return po;
    }
}

