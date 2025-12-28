package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.UserBodyInfoCreateOrUpdateDTO;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.domain.vo.UserBodyInfoVO;
import com.pbad.health.mapper.HealthUserBodyInfoMapper;
import common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户身体信息服务实现类单元测试.
 * 关联测试用例：TC-BODY-001至TC-BODY-003
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class HealthUserBodyInfoServiceImplTest {

    @Mock
    private HealthUserBodyInfoMapper userBodyInfoMapper;

    @Mock
    private IdGeneratorApi idGeneratorApi;

    @InjectMocks
    private HealthUserBodyInfoServiceImpl userBodyInfoService;

    private static final String TEST_USER_ID = "test_user_001";
    private static final String TEST_BODY_INFO_ID = "body_info_001";

    @BeforeEach
    void setUp() {
        when(idGeneratorApi.generateId()).thenReturn(TEST_BODY_INFO_ID);
    }

    // ==================== 创建/更新用户身体信息测试 ====================

    /**
     * TC-BODY-001: 创建用户身体信息-成功
     */
    @Test
    void testCreateOrUpdate_CreateSuccess() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("175.00"));
        dto.setBirthDate("1990-01-01");
        dto.setGender("MALE");
        dto.setTargetWeightKg(new BigDecimal("70.00"));
        dto.setResendEmail("test@example.com");

        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_BODY_INFO_ID, result.getId());
        assertEquals(new BigDecimal("175.00"), result.getHeightCm());
        assertEquals("MALE", result.getGender());
        assertEquals(new BigDecimal("70.00"), result.getTargetWeightKg());
        assertEquals("test@example.com", result.getResendEmail());

        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
        verify(userBodyInfoMapper, times(1)).insert(any(HealthUserBodyInfoPO.class));
        verify(idGeneratorApi, times(1)).generateId();
    }

    /**
     * TC-BODY-001: 更新用户身体信息-成功
     */
    @Test
    void testCreateOrUpdate_UpdateSuccess() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("180.00"));

        HealthUserBodyInfoPO existingPo = createTestPO();
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(existingPo);
        when(userBodyInfoMapper.update(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("180.00"), result.getHeightCm());

        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
        verify(userBodyInfoMapper, times(1)).update(any(HealthUserBodyInfoPO.class));
        verify(userBodyInfoMapper, never()).insert(any(HealthUserBodyInfoPO.class));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-参数为空
     */
    @Test
    void testCreateOrUpdate_NullDTO() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(null, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("请求参数不能为空"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-身高过小
     */
    @Test
    void testCreateOrUpdate_HeightCmTooSmall() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("99.99"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("身高必须在100.00-250.00厘米之间"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-身高过大
     */
    @Test
    void testCreateOrUpdate_HeightCmTooLarge() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("250.01"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("身高必须在100.00-250.00厘米之间"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-性别无效
     */
    @Test
    void testCreateOrUpdate_InvalidGender() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setGender("INVALID");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("性别无效"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-目标体重过小
     */
    @Test
    void testCreateOrUpdate_TargetWeightKgTooSmall() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setTargetWeightKg(new BigDecimal("19.99"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("目标体重必须在20.00-300.00公斤之间"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-目标体重过大
     */
    @Test
    void testCreateOrUpdate_TargetWeightKgTooLarge() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setTargetWeightKg(new BigDecimal("300.01"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("目标体重必须在20.00-300.00公斤之间"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-邮箱格式错误
     */
    @Test
    void testCreateOrUpdate_InvalidEmail() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setResendEmail("invalid-email");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("邮箱格式不正确"));
    }

    /**
     * TC-BODY-001-异常: 创建/更新用户身体信息-出生日期格式错误
     */
    @Test
    void testCreateOrUpdate_InvalidBirthDate() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setBirthDate("1990/01/01"); // 错误格式

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("出生日期格式错误"));
    }

    /**
     * TC-BODY-001-异常: 更新用户身体信息-没有需要更新的字段
     */
    @Test
    void testCreateOrUpdate_NoUpdateFields() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();

        HealthUserBodyInfoPO existingPo = createTestPO();
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(existingPo);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);
        });

        assertEquals("400", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("没有需要更新的字段"));
    }

    /**
     * TC-BODY-001: 创建用户身体信息-支持所有性别值
     */
    @Test
    void testCreateOrUpdate_AllGenderValues() {
        String[] genders = {"MALE", "FEMALE", "OTHER"};

        for (String gender : genders) {
            UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
            dto.setGender(gender);

            when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
            when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

            UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

            assertNotNull(result);
            assertEquals(gender, result.getGender());
        }
    }

    // ==================== 查询用户身体信息测试 ====================

    /**
     * TC-BODY-002: 根据用户ID查询用户身体信息-成功
     */
    @Test
    void testGetByUserId_Success() {
        HealthUserBodyInfoPO po = createTestPO();
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(po);

        UserBodyInfoVO result = userBodyInfoService.getByUserId(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(TEST_BODY_INFO_ID, result.getId());
        assertEquals(new BigDecimal("175.00"), result.getHeightCm());
        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
    }

    /**
     * TC-BODY-002: 根据用户ID查询用户身体信息-不存在
     */
    @Test
    void testGetByUserId_NotFound() {
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);

        UserBodyInfoVO result = userBodyInfoService.getByUserId(TEST_USER_ID);

        assertNull(result);
        verify(userBodyInfoMapper, times(1)).selectByUserId(TEST_USER_ID);
    }

    // ==================== 边界条件测试 ====================

    /**
     * TC-BODY-003: 创建用户身体信息-最小身高值
     */
    @Test
    void testCreateOrUpdate_MinHeight() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("100.00"));

        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getHeightCm());
    }

    /**
     * TC-BODY-003: 创建用户身体信息-最大身高值
     */
    @Test
    void testCreateOrUpdate_MaxHeight() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setHeightCm(new BigDecimal("250.00"));

        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("250.00"), result.getHeightCm());
    }

    /**
     * TC-BODY-003: 创建用户身体信息-最小目标体重值
     */
    @Test
    void testCreateOrUpdate_MinTargetWeight() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setTargetWeightKg(new BigDecimal("20.00"));

        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("20.00"), result.getTargetWeightKg());
    }

    /**
     * TC-BODY-003: 创建用户身体信息-最大目标体重值
     */
    @Test
    void testCreateOrUpdate_MaxTargetWeight() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setTargetWeightKg(new BigDecimal("300.00"));

        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(null);
        when(userBodyInfoMapper.insert(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.getTargetWeightKg());
    }

    /**
     * TC-BODY-003: 更新用户身体信息-清空邮箱
     */
    @Test
    void testCreateOrUpdate_ClearEmail() {
        UserBodyInfoCreateOrUpdateDTO dto = new UserBodyInfoCreateOrUpdateDTO();
        dto.setResendEmail(""); // 空字符串

        HealthUserBodyInfoPO existingPo = createTestPO();
        when(userBodyInfoMapper.selectByUserId(TEST_USER_ID)).thenReturn(existingPo);
        when(userBodyInfoMapper.update(any(HealthUserBodyInfoPO.class))).thenReturn(1);

        UserBodyInfoVO result = userBodyInfoService.createOrUpdate(dto, TEST_USER_ID);

        assertNotNull(result);
        assertEquals("", result.getResendEmail());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的PO对象
     */
    private HealthUserBodyInfoPO createTestPO() {
        HealthUserBodyInfoPO po = new HealthUserBodyInfoPO();
        po.setId(TEST_BODY_INFO_ID);
        po.setUserId(TEST_USER_ID);
        po.setHeightCm(new BigDecimal("175.00"));
        po.setBirthDate(new Date());
        po.setGender("MALE");
        po.setTargetWeightKg(new BigDecimal("70.00"));
        po.setResendEmail("test@example.com");
        po.setCreatedAt(new Date());
        po.setUpdatedAt(new Date());
        return po;
    }
}

