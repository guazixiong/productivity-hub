package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.TrainingPlanCreateDTO;
import com.pbad.health.domain.dto.TrainingPlanQueryDTO;
import com.pbad.health.domain.dto.TrainingPlanUpdateDTO;
import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 训练计划Service单元测试.
 * 关联测试用例：TC-PLAN-001至TC-PLAN-008
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthTrainingPlanServiceImplTest {

    @Mock
    private HealthTrainingPlanMapper trainingPlanMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthTrainingPlanServiceImpl trainingPlanService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_PLAN_ID = "plan_001";

    @BeforeEach
    void setUp() {
        when(idGeneratorApi.generateId()).thenReturn(TEST_PLAN_ID);
    }

    /**
     * TC-PLAN-001: 正常创建训练计划
     */
    @Test
    void testCreate_Success() {
        TrainingPlanCreateDTO createDTO = new TrainingPlanCreateDTO();
        createDTO.setPlanName("减脂计划");
        createDTO.setPlanType("减脂");
        createDTO.setTargetDurationDays(30);
        createDTO.setTargetCaloriesPerDay(500);

        when(trainingPlanMapper.insert(any(HealthTrainingPlanPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.TrainingPlanVO result = trainingPlanService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_PLAN_ID, result.getId());
        assertEquals("减脂计划", result.getPlanName());
        assertEquals("ACTIVE", result.getStatus());
        verify(trainingPlanMapper, times(1)).insert(any(HealthTrainingPlanPO.class));
    }

    /**
     * TC-PLAN-002: 创建训练计划-计划名称为空
     */
    @Test
    void testCreate_PlanNameEmpty() {
        TrainingPlanCreateDTO createDTO = new TrainingPlanCreateDTO();
        createDTO.setPlanType("减脂");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            trainingPlanService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("计划名称不能为空"));
    }

    /**
     * TC-PLAN-003: 暂停训练计划-正常暂停
     */
    @Test
    void testPause_Success() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);
        po.setStatus("ACTIVE");

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);
        when(trainingPlanMapper.update(any(HealthTrainingPlanPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.TrainingPlanVO result = trainingPlanService.pause(TEST_PLAN_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("PAUSED", result.getStatus());
        verify(trainingPlanMapper, times(1)).update(any(HealthTrainingPlanPO.class));
    }

    /**
     * TC-PLAN-004: 暂停训练计划-状态不是ACTIVE
     */
    @Test
    void testPause_InvalidStatus() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);
        po.setStatus("COMPLETED");

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            trainingPlanService.pause(TEST_PLAN_ID, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("只有进行中的训练计划可以暂停"));
    }

    /**
     * TC-PLAN-005: 恢复训练计划-正常恢复
     */
    @Test
    void testResume_Success() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);
        po.setStatus("PAUSED");

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);
        when(trainingPlanMapper.update(any(HealthTrainingPlanPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.TrainingPlanVO result = trainingPlanService.resume(TEST_PLAN_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(trainingPlanMapper, times(1)).update(any(HealthTrainingPlanPO.class));
    }

    /**
     * TC-PLAN-006: 完成训练计划-正常完成
     */
    @Test
    void testComplete_Success() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);
        po.setStatus("ACTIVE");

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);
        when(trainingPlanMapper.update(any(HealthTrainingPlanPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.TrainingPlanVO result = trainingPlanService.complete(TEST_PLAN_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(trainingPlanMapper, times(1)).update(any(HealthTrainingPlanPO.class));
    }

    /**
     * TC-PLAN-007: 删除训练计划-正常删除
     */
    @Test
    void testDelete_Success() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);
        when(trainingPlanMapper.countExerciseRecords(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(0L);
        when(trainingPlanMapper.deleteById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(1);

        assertDoesNotThrow(() -> {
            trainingPlanService.delete(TEST_PLAN_ID, TEST_USER_ID);
        });

        verify(trainingPlanMapper, times(1)).deleteById(TEST_PLAN_ID, TEST_USER_ID);
    }

    /**
     * TC-PLAN-008: 删除训练计划-有关联记录不允许删除
     */
    @Test
    void testDelete_WithRecords() {
        HealthTrainingPlanPO po = new HealthTrainingPlanPO();
        po.setId(TEST_PLAN_ID);
        po.setUserId(TEST_USER_ID);

        when(trainingPlanMapper.selectById(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(po);
        when(trainingPlanMapper.countExerciseRecords(TEST_PLAN_ID, TEST_USER_ID)).thenReturn(5L);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            trainingPlanService.delete(TEST_PLAN_ID, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("存在运动记录，无法删除"));
    }
}

