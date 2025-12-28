package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.dto.ExerciseRecordUpdateDTO;
import com.pbad.health.domain.po.HealthExerciseRecordPO;
import com.pbad.health.domain.po.HealthTrainingPlanPO;
import com.pbad.health.mapper.HealthExerciseRecordMapper;
import com.pbad.health.mapper.HealthTrainingPlanMapper;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 运动记录Service单元测试.
 * 关联测试用例：TC-EX-001至TC-EX-024
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthExerciseRecordServiceImplTest {

    @Mock
    private HealthExerciseRecordMapper exerciseRecordMapper;

    @Mock
    private HealthTrainingPlanMapper trainingPlanMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthExerciseRecordServiceImpl exerciseRecordService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "record_001";
    private static final String TEST_PLAN_ID = "plan_001";

    @BeforeEach
    void setUp() {
        // 设置ID生成器Mock
        when(idGeneratorApi.generateId()).thenReturn(TEST_RECORD_ID);
    }

    /**
     * TC-EX-001: 正常新增运动记录
     */
    @Test
    void testCreate_Success() {
        // 准备测试数据
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("跑步");
        createDTO.setExerciseDate("2025-01-15");
        createDTO.setDurationMinutes(30);
        createDTO.setCaloriesBurned(300);
        createDTO.setDistanceKm(new BigDecimal("5.5"));

        // Mock Mapper行为
        when(exerciseRecordMapper.insert(any(HealthExerciseRecordPO.class))).thenReturn(1);

        // 执行测试
        com.pbad.health.domain.vo.ExerciseRecordVO result = exerciseRecordService.create(createDTO, TEST_USER_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals("跑步", result.getExerciseType());
        assertEquals(30, result.getDurationMinutes());
        assertEquals(300, result.getCaloriesBurned());

        // 验证Mapper调用
        verify(exerciseRecordMapper, times(1)).insert(any(HealthExerciseRecordPO.class));
    }

    /**
     * TC-EX-002: 新增运动记录-运动类型为空
     */
    @Test
    void testCreate_ExerciseTypeEmpty() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setDurationMinutes(30);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exerciseRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("运动类型不能为空"));
    }

    /**
     * TC-EX-003: 新增运动记录-运动时长为空
     */
    @Test
    void testCreate_DurationMinutesEmpty() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("跑步");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exerciseRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("运动时长不能为空"));
    }

    /**
     * TC-EX-004: 新增运动记录-运动时长超出范围
     */
    @Test
    void testCreate_DurationMinutesOutOfRange() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("跑步");
        createDTO.setDurationMinutes(2000); // 超出1-1440范围

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exerciseRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("运动时长必须在1-1440分钟之间"));
    }

    /**
     * TC-EX-005: 新增运动记录-运动类型无效
     */
    @Test
    void testCreate_InvalidExerciseType() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("无效类型");
        createDTO.setDurationMinutes(30);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exerciseRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("运动类型无效"));
    }

    /**
     * TC-EX-006: 新增运动记录-默认日期为当天
     */
    @Test
    void testCreate_DefaultDate() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("跑步");
        createDTO.setDurationMinutes(30);

        when(exerciseRecordMapper.insert(any(HealthExerciseRecordPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.ExerciseRecordVO result = exerciseRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertNotNull(result.getExerciseDate());
        verify(exerciseRecordMapper, times(1)).insert(any(HealthExerciseRecordPO.class));
    }

    /**
     * TC-EX-007: 查询运动记录列表-正常分页查询
     */
    @Test
    void testQueryPage_Success() {
        ExerciseRecordQueryDTO queryDTO = new ExerciseRecordQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(20);

        List<HealthExerciseRecordPO> poList = new ArrayList<>();
        HealthExerciseRecordPO po = new HealthExerciseRecordPO();
        po.setId(TEST_RECORD_ID);
        po.setUserId(TEST_USER_ID);
        po.setExerciseType("跑步");
        po.setDurationMinutes(30);
        poList.add(po);

        when(exerciseRecordMapper.selectByPage(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(poList);
        when(exerciseRecordMapper.countByCondition(any(), any(), any(), any(), any())).thenReturn(1L);

        PageResult<?> result = exerciseRecordService.queryPage(queryDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

    /**
     * TC-EX-008: 查询运动记录详情-记录存在
     */
    @Test
    void testGetById_Success() {
        HealthExerciseRecordPO po = new HealthExerciseRecordPO();
        po.setId(TEST_RECORD_ID);
        po.setUserId(TEST_USER_ID);
        po.setExerciseType("跑步");
        po.setDurationMinutes(30);

        when(exerciseRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);

        com.pbad.health.domain.vo.ExerciseRecordVO result = exerciseRecordService.getById(TEST_RECORD_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        assertEquals("跑步", result.getExerciseType());
    }

    /**
     * TC-EX-009: 查询运动记录详情-记录不存在
     */
    @Test
    void testGetById_NotFound() {
        when(exerciseRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            exerciseRecordService.getById(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("运动记录不存在"));
    }

    /**
     * TC-EX-010: 更新运动记录-正常更新
     */
    @Test
    void testUpdate_Success() {
        HealthExerciseRecordPO existingPo = new HealthExerciseRecordPO();
        existingPo.setId(TEST_RECORD_ID);
        existingPo.setUserId(TEST_USER_ID);
        existingPo.setExerciseType("跑步");
        existingPo.setDurationMinutes(30);

        ExerciseRecordUpdateDTO updateDTO = new ExerciseRecordUpdateDTO();
        updateDTO.setDurationMinutes(45);

        when(exerciseRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);
        when(exerciseRecordMapper.update(any(HealthExerciseRecordPO.class))).thenReturn(1);

        com.pbad.health.domain.vo.ExerciseRecordVO result = exerciseRecordService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(45, result.getDurationMinutes());
        verify(exerciseRecordMapper, times(1)).update(any(HealthExerciseRecordPO.class));
    }

    /**
     * TC-EX-011: 删除运动记录-正常删除
     */
    @Test
    void testDelete_Success() {
        HealthExerciseRecordPO po = new HealthExerciseRecordPO();
        po.setId(TEST_RECORD_ID);
        po.setUserId(TEST_USER_ID);

        when(exerciseRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);
        when(exerciseRecordMapper.deleteById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(1);

        assertDoesNotThrow(() -> {
            exerciseRecordService.delete(TEST_RECORD_ID, TEST_USER_ID);
        });

        verify(exerciseRecordMapper, times(1)).deleteById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-EX-012: 批量删除运动记录-正常删除
     */
    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList(TEST_RECORD_ID, "record_002");

        when(exerciseRecordMapper.batchDeleteByIds(ids, TEST_USER_ID)).thenReturn(2);

        assertDoesNotThrow(() -> {
            exerciseRecordService.batchDelete(ids, TEST_USER_ID);
        });

        verify(exerciseRecordMapper, times(1)).batchDeleteByIds(ids, TEST_USER_ID);
    }
}

