package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.UserBodyInfoCreateOrUpdateDTO;
import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import com.pbad.health.domain.vo.UserBodyInfoVO;
import com.pbad.health.mapper.HealthUserBodyInfoMapper;
import com.pbad.health.mapper.HealthWeightRecordMapper;
import com.pbad.health.service.HealthUserBodyInfoService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 用户身体信息服务实现类.
 * 关联需求：REQ-HEALTH-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthUserBodyInfoServiceImpl implements HealthUserBodyInfoService {

    private final HealthUserBodyInfoMapper userBodyInfoMapper;
    private final HealthWeightRecordMapper weightRecordMapper;
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBodyInfoVO createOrUpdate(UserBodyInfoCreateOrUpdateDTO dto, String userId) {
        // 参数校验
        validateDTO(dto);

        // 查询是否已存在
        HealthUserBodyInfoPO existingPo = userBodyInfoMapper.selectByUserId(userId);

        Date now = new Date();

        if (existingPo != null) {
            // 更新现有记录
            boolean hasUpdate = false;
            boolean heightChanged = false;
            BigDecimal oldHeight = existingPo.getHeightCm();

            if (dto.getHeightCm() != null) {
                validateHeightCm(dto.getHeightCm());
                // 检查身高是否发生变化
                if (oldHeight == null || dto.getHeightCm().compareTo(oldHeight) != 0) {
                    heightChanged = true;
                }
                existingPo.setHeightCm(dto.getHeightCm());
                hasUpdate = true;
            }

            if (StringUtils.hasText(dto.getBirthDate())) {
                Date birthDate = parseDate(dto.getBirthDate());
                if (birthDate == null) {
                    throw new BusinessException("400", "出生日期格式错误，应为yyyy-MM-dd");
                }
                existingPo.setBirthDate(birthDate);
                hasUpdate = true;
            }

            if (StringUtils.hasText(dto.getGender())) {
                validateGender(dto.getGender());
                existingPo.setGender(dto.getGender());
                hasUpdate = true;
            }

            if (dto.getTargetWeightKg() != null) {
                validateTargetWeightKg(dto.getTargetWeightKg());
                existingPo.setTargetWeightKg(dto.getTargetWeightKg());
                hasUpdate = true;
            }

            if (dto.getResendEmail() != null) {
                if (StringUtils.hasText(dto.getResendEmail())) {
                    validateEmail(dto.getResendEmail());
                }
                existingPo.setResendEmail(dto.getResendEmail());
                hasUpdate = true;
            }

            if (!hasUpdate) {
                throw new BusinessException("400", "没有需要更新的字段");
            }

            existingPo.setUpdatedAt(now);

            int result = userBodyInfoMapper.update(existingPo);
            if (result <= 0) {
                throw new BusinessException("500", "更新用户身体信息失败");
            }

            // 如果身高发生变化，自动更新所有历史体重记录的BMI
            if (heightChanged && dto.getHeightCm() != null) {
                try {
                    int updatedCount = weightRecordMapper.batchUpdateBmiByHeight(userId, dto.getHeightCm());
                    log.info("用户{}更新身体信息成功，已自动更新{}条历史体重记录的BMI", userId, updatedCount);
                } catch (Exception e) {
                    log.error("更新历史体重记录BMI失败", e);
                    // 不抛出异常，避免影响身体信息的更新
                }
            } else {
                log.info("用户{}更新身体信息成功", userId);
            }
            return convertToVO(existingPo);
        } else {
            // 创建新记录
            String id = idGeneratorApi.generateId();

            HealthUserBodyInfoPO po = new HealthUserBodyInfoPO();
            po.setId(id);
            po.setUserId(userId);

            if (dto.getHeightCm() != null) {
                validateHeightCm(dto.getHeightCm());
                po.setHeightCm(dto.getHeightCm());
            }

            if (StringUtils.hasText(dto.getBirthDate())) {
                Date birthDate = parseDate(dto.getBirthDate());
                if (birthDate == null) {
                    throw new BusinessException("400", "出生日期格式错误，应为yyyy-MM-dd");
                }
                po.setBirthDate(birthDate);
            }

            if (StringUtils.hasText(dto.getGender())) {
                validateGender(dto.getGender());
                po.setGender(dto.getGender());
            }

            if (dto.getTargetWeightKg() != null) {
                validateTargetWeightKg(dto.getTargetWeightKg());
                po.setTargetWeightKg(dto.getTargetWeightKg());
            }

            if (StringUtils.hasText(dto.getResendEmail())) {
                validateEmail(dto.getResendEmail());
                po.setResendEmail(dto.getResendEmail());
            }

            po.setCreatedAt(now);
            po.setUpdatedAt(now);

            int result = userBodyInfoMapper.insert(po);
            if (result <= 0) {
                throw new BusinessException("500", "创建用户身体信息失败");
            }

            log.info("用户{}创建身体信息成功，ID: {}", userId, id);
            return convertToVO(po);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserBodyInfoVO getByUserId(String userId) {
        HealthUserBodyInfoPO po = userBodyInfoMapper.selectByUserId(userId);
        if (po == null) {
            return null;
        }
        return convertToVO(po);
    }

    /**
     * 校验DTO
     */
    private void validateDTO(UserBodyInfoCreateOrUpdateDTO dto) {
        if (dto == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了身高，需要校验
        if (dto.getHeightCm() != null) {
            validateHeightCm(dto.getHeightCm());
        }

        // 如果提供了性别，需要校验
        if (StringUtils.hasText(dto.getGender())) {
            validateGender(dto.getGender());
        }

        // 如果提供了目标体重，需要校验
        if (dto.getTargetWeightKg() != null) {
            validateTargetWeightKg(dto.getTargetWeightKg());
        }

        // 如果提供了邮箱，需要校验
        if (StringUtils.hasText(dto.getResendEmail())) {
            validateEmail(dto.getResendEmail());
        }
    }

    /**
     * 校验身高
     */
    private void validateHeightCm(BigDecimal heightCm) {
        if (heightCm == null) {
            return;
        }
        BigDecimal min = new BigDecimal("100.00");
        BigDecimal max = new BigDecimal("250.00");
        if (heightCm.compareTo(min) < 0 || heightCm.compareTo(max) > 0) {
            throw new BusinessException("400", "身高必须在100.00-250.00厘米之间");
        }
    }

    /**
     * 校验性别
     */
    private void validateGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return;
        }
        if (!"MALE".equalsIgnoreCase(gender) && !"FEMALE".equalsIgnoreCase(gender) && !"OTHER".equalsIgnoreCase(gender)) {
            throw new BusinessException("400", "性别无效，可选值：MALE、FEMALE、OTHER");
        }
    }

    /**
     * 校验目标体重
     */
    private void validateTargetWeightKg(BigDecimal targetWeightKg) {
        if (targetWeightKg == null) {
            return;
        }
        BigDecimal min = new BigDecimal("20.00");
        BigDecimal max = new BigDecimal("300.00");
        if (targetWeightKg.compareTo(min) < 0 || targetWeightKg.compareTo(max) > 0) {
            throw new BusinessException("400", "目标体重必须在20.00-300.00公斤之间");
        }
    }

    /**
     * 校验邮箱格式
     */
    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        // 简单的邮箱格式校验
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailPattern)) {
            throw new BusinessException("400", "邮箱格式不正确");
        }
    }

    /**
     * 解析日期字符串（yyyy-MM-dd格式）
     */
    private Date parseDate(String dateStr) {
        if (!StringUtils.hasText(dateStr)) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr.trim());
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            log.warn("解析日期失败: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 转换为VO对象
     */
    private UserBodyInfoVO convertToVO(HealthUserBodyInfoPO po) {
        UserBodyInfoVO vo = new UserBodyInfoVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setHeightCm(po.getHeightCm());
        vo.setBirthDate(po.getBirthDate());
        vo.setGender(po.getGender());
        vo.setTargetWeightKg(po.getTargetWeightKg());
        vo.setResendEmail(po.getResendEmail());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

