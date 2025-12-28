package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.WeightRecordCreateDTO;
import com.pbad.health.domain.dto.WeightRecordQueryDTO;
import com.pbad.health.domain.dto.WeightRecordUpdateDTO;
import com.pbad.health.domain.po.HealthWeightRecordPO;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.domain.vo.WeightRecordVO;
import com.pbad.health.mapper.HealthWeightRecordMapper;
import com.pbad.health.mapper.HealthUserBodyInfoMapper;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 体重记录服务实现类单元测试.
 * 关联测试用例：TC-WEIGHT-001至TC-WEIGHT-009
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthWeightRecordServiceImplTest {

    @Mock
    private HealthWeightRecordMapper weightRecordMapper;

    @Mock
    private HealthUserBodyInfoMapper userBodyInfoMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthWeightRecordServiceImpl weightRecordService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_RECORD_ID = "weight_record_001";

    @BeforeEach
    void setUp() {
        when(idGeneratorApi.generateId()).thenReturn(TEST_RECORD_ID);
    }

    // ==================== 创建体重记录测试 ====================

    /**
     * TC-WEIGHT-001: 创建体重记录-成功
     */
    @Test
    void testCreate_Success() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("70.50"));
        createDTO.setHeightCm(new BigDecimal("175.00"));
        createDTO.setRecordDate("2025-01-15");
        createDTO.setRecordTime("08:30");

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        assertEquals(new BigDecimal("70.50"), result.getWeightKg());
        assertNotNull(result.getBmi());
        assertNotNull(result.getHealthStatus());

        verify(weightRecordMapper, times(1)).insert(any(HealthWeightRecordPO.class));
        verify(idGeneratorApi, times(1)).generateId();
    }

    /**
     * TC-WEIGHT-001: 创建体重记录-使用用户身体信息中的身高计算BMI
     */
    @Test
    void testCreate_UseBodyInfoHeight() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("70.50"));
        // 不提供身高，应该使用用户身体信息中的身高

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertNotNull(result.getBmi());
        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
    }

    /**
     * TC-WEIGHT-001-异常: 创建体重记录-参数为空
     */
    @Test
    void testCreate_NullDTO() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.create(null, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("请求参数不能为空"));
    }

    /**
     * TC-WEIGHT-001-异常: 创建体重记录-体重为空
     */
    @Test
    void testCreate_NullWeightKg() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体重不能为空"));
    }

    /**
     * TC-WEIGHT-001-异常: 创建体重记录-体重过小
     */
    @Test
    void testCreate_WeightKgTooSmall() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("19.99"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体重必须在20.00-300.00公斤之间"));
    }

    /**
     * TC-WEIGHT-001-异常: 创建体重记录-体重过大
     */
    @Test
    void testCreate_WeightKgTooLarge() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("300.01"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体重必须在20.00-300.00公斤之间"));
    }

    /**
     * TC-WEIGHT-001-异常: 创建体重记录-体脂率超出范围
     */
    @Test
    void testCreate_BodyFatPercentageOutOfRange() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("70.50"));
        createDTO.setBodyFatPercentage(new BigDecimal("51.00")); // 超出范围

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.create(createDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体脂率必须在5.00-50.00%之间"));
    }

    /**
     * TC-WEIGHT-001: 创建体重记录-BMI计算（偏瘦）
     */
    @Test
    void testCreate_BmiThin() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("50.00"));
        createDTO.setHeightCm(new BigDecimal("175.00"));

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertNotNull(result.getBmi());
        assertEquals("偏瘦", result.getHealthStatus());
    }

    /**
     * TC-WEIGHT-001: 创建体重记录-BMI计算（正常）
     */
    @Test
    void testCreate_BmiNormal() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("70.00"));
        createDTO.setHeightCm(new BigDecimal("175.00"));

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("正常", result.getHealthStatus());
    }

    /**
     * TC-WEIGHT-001: 创建体重记录-BMI计算（偏胖）
     */
    @Test
    void testCreate_BmiOverweight() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("80.00"));
        createDTO.setHeightCm(new BigDecimal("175.00"));

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("偏胖", result.getHealthStatus());
    }

    /**
     * TC-WEIGHT-001: 创建体重记录-BMI计算（肥胖）
     */
    @Test
    void testCreate_BmiObese() {
        WeightRecordCreateDTO createDTO = new WeightRecordCreateDTO();
        createDTO.setWeightKg(new BigDecimal("90.00"));
        createDTO.setHeightCm(new BigDecimal("175.00"));

        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.insert(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.create(createDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("肥胖", result.getHealthStatus());
    }

    // ==================== 分页查询测试 ====================

    /**
     * TC-WEIGHT-002: 分页查询体重记录-成功
     */
    @Test
    void testQueryPage_Success() {
        WeightRecordQueryDTO queryDTO = new WeightRecordQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(20);

        HealthWeightRecordPO po = createTestPO();
        List<HealthWeightRecordPO> poList = Collections.singletonList(po);

        when(weightRecordMapper.selectByPage(anyString(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(poList);
        when(weightRecordMapper.countByCondition(anyString(), any(), any()))
                .thenReturn(1L);

        PageResult<WeightRecordVO> result = weightRecordService.queryPage(queryDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());
    }

    // ==================== 根据ID查询测试 ====================

    /**
     * TC-WEIGHT-003: 根据ID查询体重记录-成功
     */
    @Test
    void testGetById_Success() {
        HealthWeightRecordPO po = createTestPO();
        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);

        WeightRecordVO result = weightRecordService.getById(TEST_RECORD_ID, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_RECORD_ID, result.getId());
        verify(weightRecordMapper, times(1)).selectById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-WEIGHT-003-异常: 根据ID查询-ID为空
     */
    @Test
    void testGetById_EmptyId() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.getById("", TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("记录ID不能为空"));
    }

    /**
     * TC-WEIGHT-003-异常: 根据ID查询-记录不存在
     */
    @Test
    void testGetById_NotFound() {
        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.getById(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体重记录不存在"));
    }

    // ==================== 更新体重记录测试 ====================

    /**
     * TC-WEIGHT-004: 更新体重记录-成功
     */
    @Test
    void testUpdate_Success() {
        WeightRecordUpdateDTO updateDTO = new WeightRecordUpdateDTO();
        updateDTO.setWeightKg(new BigDecimal("72.00"));

        HealthWeightRecordPO existingPo = createTestPO();
        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));

        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.update(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("72.00"), result.getWeightKg());
        verify(weightRecordMapper, times(1)).update(any(HealthWeightRecordPO.class));
    }

    /**
     * TC-WEIGHT-004: 更新体重记录-重新计算BMI
     */
    @Test
    void testUpdate_RecalculateBmi() {
        WeightRecordUpdateDTO updateDTO = new WeightRecordUpdateDTO();
        updateDTO.setWeightKg(new BigDecimal("75.00"));

        HealthWeightRecordPO existingPo = createTestPO();
        HealthUserBodyInfoPO bodyInfo = new HealthUserBodyInfoPO();
        bodyInfo.setHeightCm(new BigDecimal("175.00"));

        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(bodyInfo);
        when(weightRecordMapper.update(any(HealthWeightRecordPO.class))).thenReturn(1);

        WeightRecordVO result = weightRecordService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);

        assertNotNull(result);
        assertNotNull(result.getBmi());
        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
    }

    /**
     * TC-WEIGHT-004-异常: 更新体重记录-没有需要更新的字段
     */
    @Test
    void testUpdate_NoUpdateFields() {
        WeightRecordUpdateDTO updateDTO = new WeightRecordUpdateDTO();

        HealthWeightRecordPO existingPo = createTestPO();
        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(existingPo);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.update(TEST_RECORD_ID, updateDTO, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("没有需要更新的字段"));
    }

    // ==================== 删除体重记录测试 ====================

    /**
     * TC-WEIGHT-005: 删除体重记录-成功
     */
    @Test
    void testDelete_Success() {
        HealthWeightRecordPO po = createTestPO();
        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(po);
        when(weightRecordMapper.deleteById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(1);

        weightRecordService.delete(TEST_RECORD_ID, TEST_USER_ID);

        verify(weightRecordMapper, times(1)).selectById(TEST_RECORD_ID, TEST_USER_ID);
        verify(weightRecordMapper, times(1)).deleteById(TEST_RECORD_ID, TEST_USER_ID);
    }

    /**
     * TC-WEIGHT-005-异常: 删除体重记录-记录不存在
     */
    @Test
    void testDelete_NotFound() {
        when(weightRecordMapper.selectById(TEST_RECORD_ID, TEST_USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.delete(TEST_RECORD_ID, TEST_USER_ID);
        });

        assertEquals("404", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("体重记录不存在"));
    }

    // ==================== 批量删除测试 ====================

    /**
     * TC-WEIGHT-006: 批量删除体重记录-成功
     */
    @Test
    void testBatchDelete_Success() {
        List<String> ids = Arrays.asList(TEST_RECORD_ID, "weight_record_002");
        when(weightRecordMapper.batchDeleteByIds(ids, TEST_USER_ID)).thenReturn(2);

        weightRecordService.batchDelete(ids, TEST_USER_ID);

        verify(weightRecordMapper, times(1)).batchDeleteByIds(ids, TEST_USER_ID);
    }

    /**
     * TC-WEIGHT-006-异常: 批量删除-ID列表为空
     */
    @Test
    void testBatchDelete_EmptyIds() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.batchDelete(Collections.emptyList(), TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("记录ID列表不能为空"));
    }

    /**
     * TC-WEIGHT-006-异常: 批量删除-超过100条
     */
    @Test
    void testBatchDelete_ExceedsMax() {
        List<String> ids = Collections.nCopies(101, "record_id");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            weightRecordService.batchDelete(ids, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("批量删除最多支持100条记录"));
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的PO对象
     */
    private HealthWeightRecordPO createTestPO() {
        HealthWeightRecordPO po = new HealthWeightRecordPO();
        po.setId(TEST_RECORD_ID);
        po.setUserId(TEST_USER_ID);
        po.setRecordDate(new Date());
        po.setRecordTime(new Date());
        po.setWeightKg(new BigDecimal("70.50"));
        po.setHeightCm(new BigDecimal("175.00"));
        po.setBmi(new BigDecimal("23.02"));
        po.setHealthStatus("正常");
        po.setBodyFatPercentage(new BigDecimal("15.00"));
        po.setMuscleMassKg(new BigDecimal("60.00"));
        po.setNotes("测试备注");
        po.setCreatedAt(new Date());
        po.setUpdatedAt(new Date());
        return po;
    }
}

