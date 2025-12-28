package com.pbad.health.controller;

import com.pbad.health.domain.dto.WeightRecordCreateDTO;
import com.pbad.health.domain.dto.WeightRecordQueryDTO;
import com.pbad.health.domain.dto.WeightRecordUpdateDTO;
import com.pbad.health.domain.vo.WeightRecordVO;
import com.pbad.health.service.HealthWeightRecordService;
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
 * 体重记录Controller集成测试.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthWeightRecordControllerTest {

    @Mock
    private HealthWeightRecordService weightRecordService;

    @InjectMocks
    private HealthWeightRecordController weightRecordController;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "weight_001";

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

    @Test
    void testQueryPage_Success() {
        WeightRecordQueryDTO queryDTO = new WeightRecordQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        WeightRecordVO vo = createTestWeightRecordVO();
        PageResult<WeightRecordVO> pageResult = new PageResult<>();
        pageResult.setItems(Arrays.asList(vo));
        pageResult.setTotal(1L);

        when(weightRecordService.queryPage(any(WeightRecordQueryDTO.class), eq(TEST_USER_ID)))
                .thenReturn(pageResult);

        ApiResponse<PageResult<WeightRecordVO>> response = weightRecordController.queryPage(queryDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getItems().size());
    }

    @Test
    void testCreate_Success() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setRecordDate("2025-01-15");
        createDTO.setWeightKg(new BigDecimal("70.5"));

        WeightRecordVO vo = createTestWeightRecordVO();
        when(weightRecordService.create(any(WeightRecordCreateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<WeightRecordVO> response = weightRecordController.create(createDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("创建成功", response.getMessage());
    }

    @Test
    void testUpdate_Success() {
        WeightRecordUpdateDTO updateDTO = new WeightRecordUpdateDTO();
        updateDTO.setWeightKg(new BigDecimal("71.0"));

        WeightRecordVO vo = createTestWeightRecordVO();
        when(weightRecordService.update(eq(TEST_RECORD_ID), any(WeightRecordUpdateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<WeightRecordVO> response = weightRecordController.update(TEST_RECORD_ID, updateDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("更新成功", response.getMessage());
    }

    @Test
    void testDelete_Success() {
        doNothing().when(weightRecordService).delete(TEST_RECORD_ID, TEST_USER_ID);

        ApiResponse<Void> response = weightRecordController.delete(TEST_RECORD_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("删除成功", response.getMessage());
    }

    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList("weight_001", "weight_002");
        doNothing().when(weightRecordService).batchDelete(ids, TEST_USER_ID);

        ApiResponse<Void> response = weightRecordController.batchDelete(ids);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("批量删除成功", response.getMessage());
    }

    private WeightRecordVO createTestWeightRecordVO() {
        WeightRecordVO vo = new WeightRecordVO();
        vo.setId(TEST_RECORD_ID);
        vo.setUserId(TEST_USER_ID);
        try {
            vo.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        vo.setWeightKg(new BigDecimal("70.5"));
        return vo;
    }
}

