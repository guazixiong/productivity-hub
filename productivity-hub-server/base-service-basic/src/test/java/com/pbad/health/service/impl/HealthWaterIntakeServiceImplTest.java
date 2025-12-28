package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.WaterIntakeCreateDTO;
import com.pbad.health.domain.dto.WaterIntakeQueryDTO;
import com.pbad.health.domain.dto.WaterIntakeUpdateDTO;
import com.pbad.health.domain.po.HealthWaterIntakePO;
import com.pbad.health.domain.vo.WaterIntakeVO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import common.core.domain.PageResult;
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
 * 饮水记录服务实现类单元测试.
 * 关联测试用例：TC-WATER-001至TC-WATER-005
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthWaterIntakeServiceImplTest {

    @Mock
    private HealthWaterIntakeMapper waterIntakeMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthWaterIntakeServiceImpl waterIntakeService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "water_record_001";

    @BeforeEach
    void setUp() {
        // 设置ID生成器默认返回值
        when(idGeneratorApi.generateId()).thenReturn(TEST_RECORD_ID);
    }

    // ==================== 创建饮水记录测试 ====================

    /**
     * TC-WATER-001: 创建饮水记录-成功
     */
    @Test
    void testCreate_Success() {
        // 准备测试数据
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(500);
        createDTO.setIntakeDate("2025-01-15");
        createDTO.setIntakeTime("10:30");

        // Mock Mapper行为
        when(waterIntakeMapper.insert(any(HealthWaterIntakePO.class))).thenReturn(1);

        // 执行测试
        WaterIntakeVO result = waterIntakeService.create(createDTO, TEST_USER_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals("白开水", result.getWaterType());
        assertEquals(500, result.getVolumeMl());

        // 验证Mapper调用
        verify(waterIntakeMapper, times(1)).insert(any(HealthWaterIntakePO.class));
        verify(idGeneratorApi, times(1)).generateId();
    }

    /**
     * TC-WATER-001-边界: 创建饮水记录-使用默认日期和时间
     */
    @Test
    void testCreate_WithDefaultDateAndTime() {
        // 准备测试数据（不提供日期和时间）
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("矿泉水");
        createDTO.setVolumeMl(300);

        // Mock Mapper行为
        when(waterIntakeMapper.insert(any(HealthWaterIntakePO.class))).thenReturn(1);

        // 执行测试
        WaterIntakeVO result = waterIntakeService.create(createDTO, TEST_USER_ID);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getIntakeDate());
        assertNotNull(result.getIntakeTime());

        // 验证Mapper调用
        verify(waterIntakeMapper, times(1)).insert(any(HealthWaterIntakePO.class));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-参数为空
     */
    @Test
    void testCreate_NullDTO() {
        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(null, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("请求参数不能为空"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-饮水类型为空
     */
    @Test
    void testCreate_EmptyWaterType() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setVolumeMl(500);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水类型不能为空"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-饮水类型无效
     */
    @Test
    void testCreate_InvalidWaterType() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("无效类型");
        createDTO.setVolumeMl(500);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水类型无效"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-饮水量为空
     */
    @Test
    void testCreate_NullVolumeMl() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水量不能为空"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-饮水量超出范围（小于1）
     */
    @Test
    void testCreate_VolumeMlTooSmall() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水量必须在1-5000毫升之间"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-饮水量超出范围（大于5000）
     */
    @Test
    void testCreate_VolumeMlTooLarge() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(5001);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水量必须在1-5000毫升之间"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-日期格式错误
     */
    @Test
    void testCreate_InvalidDateFormat() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(500);
        createDTO.setIntakeDate("2025/01/15"); // 错误格式

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水日期格式错误"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-时间格式错误
     */
    @Test
    void testCreate_InvalidTimeFormat() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(500);
        createDTO.setIntakeTime("10:30:00"); // 错误格式

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水时间格式错误"));
    }

    /**
     * TC-WATER-001-异常: 创建饮水记录-数据库插入失败
     */
    @Test
    void testCreate_DatabaseInsertFailed() {
        WaterIntakeCreateDTO createDTO = new WaterIntakeCreateDTO();
        createDTO.setWaterType("白开水");
        createDTO.setVolumeMl(500);

        when(waterIntakeMapper.insert(any(HealthWaterIntakePO.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("500", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("创建饮水记录失败"));
    }

    // ==================== 分页查询测试 ====================

    /**
     * TC-WATER-002: 分页查询饮水记录-成功
     */
    @Test
    void testQueryPage_Success() {
        // 准备测试数据
        WaterIntakeQueryDTO queryDTO = new WaterIntakeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(20);

        HealthWaterIntakePO po = createTestPO();
        List<HealthWaterIntakePO> poList = Collections.singletonList(po);

        // Mock Mapper行为
        when(waterIntakeMapper.selectByPage(anyString(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(poList);
        when(waterIntakeMapper.countByCondition(anyString(), any(), any(), any()))
                .thenReturn(1L);

        // 执行测试
        PageResult<WaterIntakeVO> result = waterIntakeService.queryPage(queryDTO, TEST_USER_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());

        // 验证Mapper调用
        verify(waterIntakeMapper, times(1)).selectByPage(anyString(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt());
        verify(waterIntakeMapper, times(1)).countByCondition(anyString(), any(), any(), any());
    }

    /**
     * TC-WATER-002-边界: 分页查询-使用默认分页参数
     */
    @Test
    void testQueryPage_WithDefaultPagination() {
        WaterIntakeQueryDTO queryDTO = new WaterIntakeQueryDTO();

        when(waterIntakeMapper.selectByPage(anyString(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(waterIntakeMapper.countByCondition(anyString(), any(), any(), any()))
                .thenReturn(0L);

        PageResult<WaterIntakeVO> result = waterIntakeService.queryPage(queryDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
    }

    /**
     * TC-WATER-002-边界: 分页查询-页面大小超过100
     */
    @Test
    void testQueryPage_PageSizeExceedsMax() {
        WaterIntakeQueryDTO queryDTO = new WaterIntakeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(200); // 超过100

        when(waterIntakeMapper.selectByPage(anyString(), any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(waterIntakeMapper.countByCondition(anyString(), any(), any(), any()))
                .thenReturn(0L);

        PageResult<WaterIntakeVO> result = waterIntakeService.queryPage(queryDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(100, result.getPageSize()); // 应该被限制为100
    }

    /**
     * TC-WATER-002-异常: 分页查询-开始日期格式错误
     */
    @Test
    void testQueryPage_InvalidStartDate() {
        WaterIntakeQueryDTO queryDTO = new WaterIntakeQueryDTO();
        queryDTO.setStartDate("2025/01/15"); // 错误格式

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.queryPage(queryDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("开始日期格式错误"));
    }

    // ==================== 根据ID查询测试 ====================

    /**
     * TC-WATER-003: 根据ID查询饮水记录-成功
     */
    @Test
    void testGetById_Success() {
        HealthWaterIntakePO po = createTestPO();
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);

        WaterIntakeVO result = waterIntakeService.getById(TEST_RECORD_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        verify(waterIntakeMapper, times(1)).selectById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-WATER-003-异常: 根据ID查询-ID为空
     */
    @Test
    void testGetById_EmptyId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.getById("", TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("记录ID不能为空"));
    }

    /**
     * TC-WATER-003-异常: 根据ID查询-记录不存在
     */
    @Test
    void testGetById_NotFound() {
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.getById(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水记录不存在"));
    }

    // ==================== 更新饮水记录测试 ====================

    /**
     * TC-WATER-004: 更新饮水记录-成功
     */
    @Test
    void testUpdate_Success() {
        WaterIntakeUpdateDTO updateDTO = new WaterIntakeUpdateDTO();
        updateDTO.setVolumeMl(600);

        HealthWaterIntakePO existingPo = createTestPO();
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);
        when(waterIntakeMapper.update(any(HealthWaterIntakePO.class))).thenReturn(1);

        WaterIntakeVO result = waterIntakeService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(600, result.getVolumeMl());
        verify(waterIntakeMapper, times(1)).selectById(TEST_RECORD_ID, TEST_USER_ID);
        verify(waterIntakeMapper, times(1)).update(any(HealthWaterIntakePO.class));
    }

    /**
     * TC-WATER-004-异常: 更新饮水记录-没有需要更新的字段
     */
    @Test
    void testUpdate_NoUpdateFields() {
        WaterIntakeUpdateDTO updateDTO = new WaterIntakeUpdateDTO();

        HealthWaterIntakePO existingPo = createTestPO();
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("没有需要更新的字段"));
    }

    // ==================== 删除饮水记录测试 ====================

    /**
     * TC-WATER-005: 删除饮水记录-成功
     */
    @Test
    void testDelete_Success() {
        HealthWaterIntakePO po = createTestPO();
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);
        when(waterIntakeMapper.deleteById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(1);

        waterIntakeService.delete(TEST_RECORD_ID, TEST_USER_ID);

        verify(waterIntakeMapper, times(1)).selectById(TEST_RECORD_ID, TEST_USER_ID);
        verify(waterIntakeMapper, times(1)).deleteById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-WATER-005-异常: 删除饮水记录-记录不存在
     */
    @Test
    void testDelete_NotFound() {
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.delete(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("饮水记录不存在"));
    }

    /**
     * TC-WATER-005-异常: 删除饮水记录-数据库删除失败
     */
    @Test
    void testDelete_DatabaseDeleteFailed() {
        HealthWaterIntakePO po = createTestPO();
        when(waterIntakeMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);
        when(waterIntakeMapper.deleteById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.delete(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("500", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("删除饮水记录失败"));
    }

    // ==================== 批量删除测试 ====================

    /**
     * TC-WATER-005-扩展: 批量删除饮水记录-成功
     */
    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList(TEST_RECORD_ID, "water_record_002");
        when(waterIntakeMapper.batchDeleteByIds(ids, TEST_USER_ID)).thenReturn(2);

        waterIntakeService.batchDelete(ids, TEST_USER_ID);

        verify(waterIntakeMapper, times(1)).batchDeleteByIds(ids, TEST_USER_ID);
    }

    /**
     * TC-WATER-005-异常: 批量删除-ID列表为空
     */
    @Test
    void testBatchDelete_EmptyIds() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            waterIntakeService.batchDelete(Collections.emptyList(), TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("记录ID列表不能为空"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的PO对象
     */
    private HealthWaterIntakePO createTestPO() {
        HealthWaterIntakePO po = new HealthWaterIntakePO();
        po.setId(TEST_RECORD_ID);
        po.setUserId(TEST_USER_ID);
        po.setIntakeDate(new Date());
        po.setIntakeTime(new Date());
        po.setWaterType("白开水");
        po.setVolumeMl(500);
        po.setNotes("测试备注");
        po.setCreatedAt(new Date());
        po.setUpdatedAt(new Date());
        return po;
    }
}

