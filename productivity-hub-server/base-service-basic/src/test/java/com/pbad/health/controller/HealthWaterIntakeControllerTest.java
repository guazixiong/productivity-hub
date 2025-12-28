package com.pbad.health.controller;

import com.pbad.health.domain.dto.WaterIntakeCreateDTO;
import com.pbad.health.domain.dto.WaterIntakeQueryDTO;
import com.pbad.health.domain.dto.WaterIntakeUpdateDTO;
import com.pbad.health.domain.vo.WaterIntakeVO;
import com.pbad.health.service.HealthWaterIntakeService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 饮水记录Controller集成测试.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthWaterIntakeControllerTest {

    @Mock
    private HealthWaterIntakeService waterIntakeService;

    @InjectMocks
    private HealthWaterIntakeController waterIntakeController;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "water_001";

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
        WaterIntakeQueryDTO queryDTO = new WaterIntakeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        WaterIntakeVO vo = createTestWaterIntakeVO();
        PageResult<WaterIntakeVO> pageResult = new PageResult<>();
        pageResult.setItems(Arrays.asList(vo));
        pageResult.setTotal(1L);

        when(waterIntakeService.queryPage(any(WaterIntakeQueryDTO.class), eq(TEST_USER_ID)))
                .thenReturn(pageResult);

        ApiResponse<PageResult<WaterIntakeVO>> response = waterIntakeController.queryPage(queryDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getItems().size());
    }

    @Test
    void testCreate_Success() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setIntakeDate("2025-01-15");
        createDTO.setVolumeMl(300);

        WaterIntakeVO vo = createTestWaterIntakeVO();
        when(waterIntakeService.create(any(WaterIntakeCreateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<WaterIntakeVO> response = waterIntakeController.create(createDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("创建成功", response.getMessage());
    }

    @Test
    void testUpdate_Success() {
        WaterIntakeUpdateDTO updateDTO = new WaterIntakeUpdateDTO();
        updateDTO.setVolumeMl(500);

        WaterIntakeVO vo = createTestWaterIntakeVO();
        when(waterIntakeService.update(eq(TEST_RECORD_ID), any(WaterIntakeUpdateDTO.class), eq(TEST_USER_ID)))
                .thenReturn(vo);

        ApiResponse<WaterIntakeVO> response = waterIntakeController.update(TEST_RECORD_ID, updateDTO);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("更新成功", response.getMessage());
    }

    @Test
    void testDelete_Success() {
        doNothing().when(waterIntakeService).delete(TEST_RECORD_ID, TEST_USER_ID);

        ApiResponse<Void> response = waterIntakeController.delete(TEST_RECORD_ID);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("删除成功", response.getMessage());
    }

    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList("water_001", "water_002");
        doNothing().when(waterIntakeService).batchDelete(ids, TEST_USER_ID);

        ApiResponse<Void> response = waterIntakeController.batchDelete(ids);

        assertNotNull(response);
        assertTrue(ApiResponse.isSuccess(response));
        assertEquals("批量删除成功", response.getMessage());
    }

    private WaterIntakeVO createTestWaterIntakeVO() {
        WaterIntakeVO vo = new WaterIntakeVO();
        vo.setId(TEST_RECORD_ID);
        vo.setUserId(TEST_USER_ID);
        try {
            vo.setIntakeDate(new SimpleDateFormat("yyyy-MM-dd").parse("2025-01-15"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        vo.setVolumeMl(300);
        vo.setWaterType("白开水");
        return vo;
    }
}

