package com.pbad.health.controller;

import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.dto.ExerciseRecordUpdateDTO;
import com.pbad.health.domain.vo.ExerciseRecordVO;
import com.pbad.health.service.HealthExerciseRecordService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.web.context.RequestUser;
import common.web.context.RequestUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 运动记录Controller集成测试.
 * 关联测试用例：TC-EX-CONTROLLER-001至TC-EX-CONTROLLER-010
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthExerciseRecordControllerTest {

    @Mock
    private HealthExerciseRecordService exerciseRecordService;

    @InjectMocks
    private HealthExerciseRecordController exerciseRecordController;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "record_001";

    @BeforeEach
    void setUp() {
        RequestUserContext.set(RequestUser.builder()
                .userId(TEST_USER_ID)
                .username("test_user")
                .build());
    }

    @AfterEach
    void tearDown() {
        RequestUserContext.clear();
    }

    /**
     * TC-EX-CONTROLLER-001: 查询运动记录列表-成功
     */
    @Test
    void testQueryPage_Success() {
        ExerciseRecordQueryDTO queryDTO = new ExerciseRecordQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        ExerciseRecordVO vo = createTestExerciseRecordVO();
        PageResult<ExerciseRecordVO> pageResult = new PageResult<>();
        pageResult.setItems(Arrays.asList(vo));
        pageResult.setTotal(1L);

        when(exerciseRecordService.queryPage(any(ExerciseRecordQueryDTO.class), eq(TEST_USER_ID)))
                .thenReturn(pageResult);

        ApiResponse<PageResult<ExerciseRecordVO>> response = exerciseRecordController.queryPage(queryDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getItems().size());
        verify(exerciseRecordService, times(1)).queryPage(any(), eq(TEST_USER_ID));
    }

    /**
     * TC-EX-CONTROLLER-002: 查询运动记录详情-成功
     */
    @Test
    void testGetById_Success() {
        ExerciseRecordVO vo = createTestExerciseRecordVO();
        when(exerciseRecordService.getById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(vo);

        ApiResponse<ExerciseRecordVO> response = exerciseRecordController.getById(TEST_RECORD_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertNotNull(response.getData());
        assertEquals(TEST_RECORD_ID, response.getData().getId());
        verify(exerciseRecordService, times(1)).getById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-EX-CONTROLLER-003: 新增运动记录-成功
     */
    @Test
    void testCreate_Success() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        createDTO.setExerciseType("跑步");
        createDTO.setExerciseDate("2025-01-15");
        createDTO.setDurationMinutes(30);

        ExerciseRecordVO vo = createTestExerciseRecordVO();
        when(exerciseRecordService.create(any(ExerciseRecordCreateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<ExerciseRecordVO> response = exerciseRecordController.create(createDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("创建成功", response.getMessage());
        assertNotNull(response.getData());
        verify(exerciseRecordService, times(1)).create(any(), eq(TEST_USER_ID));
    }

    /**
     * TC-EX-CONTROLLER-004: 更新运动记录-成功
     */
    @Test
    void testUpdate_Success() {
        ExerciseRecordUpdateDTO updateDTO = new ExerciseRecordUpdateDTO();
        updateDTO.setDurationMinutes(45);

        ExerciseRecordVO vo = createTestExerciseRecordVO();
        when(exerciseRecordService.update(eq(TEST_RECORD_ID), any(ExerciseRecordUpdateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<ExerciseRecordVO> response = exerciseRecordController.update(TEST_RECORD_ID, updateDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("更新成功", response.getMessage());
        verify(exerciseRecordService, times(1)).update(eq(TEST_RECORD_ID), any(), eq(TEST_USER_ID));
    }

    /**
     * TC-EX-CONTROLLER-005: 删除运动记录-成功
     */
    @Test
    void testDelete_Success() {
        doNothing().when(exerciseRecordService).delete(TEST_RECORD_ID, TEST_USER_ID);

        ApiResponse<Void> response = exerciseRecordController.delete(TEST_RECORD_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("删除成功", response.getMessage());
        verify(exerciseRecordService, times(1)).delete(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-EX-CONTROLLER-006: 批量删除运动记录-成功
     */
    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList("record_001", "record_002");
        doNothing().when(exerciseRecordService).batchDelete(ids, TEST_USER_ID);

        ApiResponse<Void> response = exerciseRecordController.batchDelete(ids);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("批量删除成功", response.getMessage());
        verify(exerciseRecordService, times(1)).batchDelete(ids, TEST_USER_ID);
    }

    /**
     * TC-EX-CONTROLLER-007: 未登录时访问接口
     */
    @Test
    void testQueryPage_Unauthorized() {
        RequestUserContext.clear();

        ExerciseRecordQueryDTO queryDTO = new ExerciseRecordQueryDTO();
        ApiResponse<PageResult<ExerciseRecordVO>> response = exerciseRecordController.queryPage(queryDTO);

        assertNotNull(response);
        assertFalse(ApiResponse.isSuccess(response));
        assertTrue(response.getMessage().contains("未登录") || response.getMessage().contains("401"));
        verify(exerciseRecordService, never()).queryPage(any(), any());
    }

    /**
     * TC-EX-CONTROLLER-008: Service层抛出异常
     */
    @Test
    void testCreate_ServiceException() {
        ExerciseRecordCreateDTO createDTO = new ExerciseRecordCreateDTO();
        when(exerciseRecordService.create(any(), eq(TEST_USER_ID)))
                .thenThrow(new RuntimeException("数据库错误"));

        ApiResponse<ExerciseRecordVO> response = exerciseRecordController.create(createDTO);

        assertNotNull(response);
        assertFalse(ApiResponse.isSuccess(response));
        assertTrue(response.getMessage().contains("失败"));
    }

    /**
     * 创建测试用的ExerciseRecordVO
     */
    private ExerciseRecordVO createTestExerciseRecordVO() {
        ExerciseRecordVO vo = new ExerciseRecordVO();
        vo.setId(TEST_RECORD_ID);
        vo.setUserId(TEST_USER_ID);
        vo.setExerciseType("跑步");
        try {
            vo.setExerciseDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        vo.setDurationMinutes(30);
        vo.setCaloriesBurned(300);
        vo.setDistanceKm(new BigDecimal("5.5"));
        return vo;
    }
}

