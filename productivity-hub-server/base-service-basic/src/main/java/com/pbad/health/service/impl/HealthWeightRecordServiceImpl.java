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
import com.pbad.health.service.HealthWeightRecordService;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 体重记录服务实现类.
 * 关联需求：REQ-HEALTH-005
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWeightRecordServiceImpl implements HealthWeightRecordService {

    private final HealthWeightRecordMapper weightRecordMapper;
    private final HealthUserBodyInfoMapper userBodyInfoMapper;
    private final IdGeneratorApi idGeneratorApi;

    // 排序字段白名单
    private static final String[] SORT_FIELDS = {
            "recordDate", "recordTime", "weightKg", "bmi"
    };

    // BMI健康状态阈值
    private static final BigDecimal BMI_THIN = new BigDecimal("18.5");
    private static final BigDecimal BMI_NORMAL = new BigDecimal("24");
    private static final BigDecimal BMI_OVERWEIGHT = new BigDecimal("28");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeightRecordVO create(WeightRecordCreateDTO createDTO, String userId) {
        // 参数校验
        validateCreateDTO(createDTO);

        // 生成ID
        String id = idGeneratorApi.generateId();

        // 构建PO对象
        HealthWeightRecordPO po = new HealthWeightRecordPO();
        po.setId(id);
        po.setUserId(userId);

        // 处理记录日期（默认当天）
        if (StringUtils.hasText(createDTO.getRecordDate())) {
            Date recordDate = parseDate(createDTO.getRecordDate());
            if (recordDate == null) {
                throw new BusinessException("400", "记录日期格式错误，应为yyyy-MM-dd");
            }
            po.setRecordDate(recordDate);
        } else {
            // 默认当天
            po.setRecordDate(new Date());
        }

        // 处理记录时间（默认当前时间）
        if (StringUtils.hasText(createDTO.getRecordTime())) {
            Date recordTime = parseTime(createDTO.getRecordTime());
            if (recordTime == null) {
                throw new BusinessException("400", "记录时间格式错误，应为HH:mm");
            }
            po.setRecordTime(recordTime);
        } else {
            // 默认当前时间
            po.setRecordTime(new Date());
        }

        po.setWeightKg(createDTO.getWeightKg());
        po.setBodyFatPercentage(createDTO.getBodyFatPercentage());
        po.setMuscleMassKg(createDTO.getMuscleMassKg());
        po.setHeightCm(createDTO.getHeightCm());
        po.setNotes(createDTO.getNotes());

        // 计算BMI和健康状态
        calculateBmiAndHealthStatus(po, userId);

        // 设置创建时间和更新时间
        Date now = new Date();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        // 插入数据库
        int result = weightRecordMapper.insert(po);
        if (result <= 0) {
            throw new BusinessException("500", "创建体重记录失败");
        }

        log.info("用户{}创建体重记录成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WeightRecordVO> queryPage(WeightRecordQueryDTO queryDTO, String userId) {
        // 参数校验和默认值设置
        int pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0
                ? queryDTO.getPageNum() : 1;
        int pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0
                ? Math.min(queryDTO.getPageSize(), 100) : 20;

        // 解析日期
        Date startDate = null;
        Date endDate = null;
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            startDate = parseDate(queryDTO.getStartDate());
            if (startDate == null) {
                throw new BusinessException("400", "开始日期格式错误，应为yyyy-MM-dd");
            }
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            endDate = parseDate(queryDTO.getEndDate());
            if (endDate == null) {
                throw new BusinessException("400", "结束日期格式错误，应为yyyy-MM-dd");
            }
        }

        // 校验排序字段
        String sortField = queryDTO.getSortField();
        if (!StringUtils.hasText(sortField)) {
            sortField = "recordDate";
        } else {
            boolean valid = false;
            for (String field : SORT_FIELDS) {
                if (field.equals(sortField)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                sortField = "recordDate";
            }
        }

        // 校验排序方向
        String sortOrder = queryDTO.getSortOrder();
        if (!StringUtils.hasText(sortOrder) || (!"ASC".equalsIgnoreCase(sortOrder) && !"DESC".equalsIgnoreCase(sortOrder))) {
            sortOrder = "DESC";
        }

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<HealthWeightRecordPO> poList = weightRecordMapper.selectByPage(
                userId,
                startDate,
                endDate,
                sortField,
                sortOrder,
                offset,
                pageSize
        );

        // 统计总数
        long total = weightRecordMapper.countByCondition(
                userId,
                startDate,
                endDate
        );

        // 转换为VO列表
        List<WeightRecordVO> voList = poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<WeightRecordVO> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setItems(voList);

        return pageResult;
    }

    @Override
    @Transactional(readOnly = true)
    public WeightRecordVO getById(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        HealthWeightRecordPO po = weightRecordMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "体重记录不存在");
        }

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WeightRecordVO update(String id, WeightRecordUpdateDTO updateDTO, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        // 查询原记录（验证数据归属）
        HealthWeightRecordPO existingPo = weightRecordMapper.selectById(id, userId);
        if (existingPo == null) {
            throw new BusinessException("404", "体重记录不存在");
        }

        // 参数校验
        validateUpdateDTO(updateDTO);

        // 更新字段
        boolean hasUpdate = false;

        if (StringUtils.hasText(updateDTO.getRecordDate())) {
            Date recordDate = parseDate(updateDTO.getRecordDate());
            if (recordDate == null) {
                throw new BusinessException("400", "记录日期格式错误，应为yyyy-MM-dd");
            }
            existingPo.setRecordDate(recordDate);
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getRecordTime())) {
            Date recordTime = parseTime(updateDTO.getRecordTime());
            if (recordTime == null) {
                throw new BusinessException("400", "记录时间格式错误，应为HH:mm");
            }
            existingPo.setRecordTime(recordTime);
            hasUpdate = true;
        }

        if (updateDTO.getWeightKg() != null) {
            validateWeightKg(updateDTO.getWeightKg());
            existingPo.setWeightKg(updateDTO.getWeightKg());
            hasUpdate = true;
        }

        if (updateDTO.getBodyFatPercentage() != null) {
            validateBodyFatPercentage(updateDTO.getBodyFatPercentage());
            existingPo.setBodyFatPercentage(updateDTO.getBodyFatPercentage());
            hasUpdate = true;
        }

        if (updateDTO.getMuscleMassKg() != null) {
            existingPo.setMuscleMassKg(updateDTO.getMuscleMassKg());
            hasUpdate = true;
        }

        if (updateDTO.getHeightCm() != null) {
            validateHeightCm(updateDTO.getHeightCm());
            existingPo.setHeightCm(updateDTO.getHeightCm());
            hasUpdate = true;
        }

        if (updateDTO.getNotes() != null) {
            existingPo.setNotes(updateDTO.getNotes());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BusinessException("400", "没有需要更新的字段");
        }

        // 重新计算BMI和健康状态（如果体重或身高发生变化）
        if (updateDTO.getWeightKg() != null || updateDTO.getHeightCm() != null) {
            calculateBmiAndHealthStatus(existingPo, userId);
        }

        // 设置更新时间
        existingPo.setUpdatedAt(new Date());

        // 更新数据库
        int result = weightRecordMapper.update(existingPo);
        if (result <= 0) {
            throw new BusinessException("500", "更新体重记录失败");
        }

        log.info("用户{}更新体重记录成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(existingPo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        // 验证数据归属
        HealthWeightRecordPO po = weightRecordMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "体重记录不存在");
        }

        // 删除记录
        int result = weightRecordMapper.deleteById(id, userId);
        if (result <= 0) {
            throw new BusinessException("500", "删除体重记录失败");
        }

        log.info("用户{}删除体重记录成功，ID: {}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids, String userId) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("400", "记录ID列表不能为空");
        }

        if (ids.size() > 100) {
            throw new BusinessException("400", "批量删除最多支持100条记录");
        }

        // 批量删除
        int result = weightRecordMapper.batchDeleteByIds(ids, userId);
        if (result <= 0) {
            throw new BusinessException("500", "批量删除体重记录失败");
        }

        log.info("用户{}批量删除体重记录成功，删除数量: {}", userId, result);
    }

    /**
     * 计算BMI和健康状态
     * 优先级：优先使用用户身体信息中的身高，其次使用记录中的身高
     *
     * @param po     体重记录PO
     * @param userId 用户ID
     */
    private void calculateBmiAndHealthStatus(HealthWeightRecordPO po, String userId) {
        if (po.getWeightKg() == null) {
            po.setBmi(null);
            po.setHealthStatus(null);
            return;
        }

        // 获取身高（优先使用用户身体信息中的身高）
        BigDecimal heightCm = null;
        HealthUserBodyInfoPO bodyInfo = userBodyInfoMapper.selectByUserId(userId);
        if (bodyInfo != null && bodyInfo.getHeightCm() != null) {
            heightCm = bodyInfo.getHeightCm();
        } else if (po.getHeightCm() != null) {
            heightCm = po.getHeightCm();
        }

        // 如果身高为空，无法计算BMI
        if (heightCm == null) {
            po.setBmi(null);
            po.setHealthStatus(null);
            return;
        }

        // 计算BMI：BMI = 体重(kg) / 身高(m)²
        // 身高从厘米转换为米
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal heightM2 = heightM.multiply(heightM);
        BigDecimal bmi = po.getWeightKg().divide(heightM2, 2, RoundingMode.HALF_UP);

        po.setBmi(bmi);

        // 判断健康状态
        po.setHealthStatus(determineHealthStatus(bmi));
    }

    /**
     * 判断健康状态
     *
     * @param bmi BMI值
     * @return 健康状态（偏瘦、正常、偏胖、肥胖）
     */
    private String determineHealthStatus(BigDecimal bmi) {
        if (bmi == null) {
            return null;
        }

        int compareThin = bmi.compareTo(BMI_THIN);
        int compareNormal = bmi.compareTo(BMI_NORMAL);
        int compareOverweight = bmi.compareTo(BMI_OVERWEIGHT);

        if (compareThin < 0) {
            return "偏瘦";
        } else if (compareNormal < 0) {
            return "正常";
        } else if (compareOverweight < 0) {
            return "偏胖";
        } else {
            return "肥胖";
        }
    }

    /**
     * 校验创建DTO
     */
    private void validateCreateDTO(WeightRecordCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 校验体重（必填）
        if (createDTO.getWeightKg() == null) {
            throw new BusinessException("400", "体重不能为空");
        }
        validateWeightKg(createDTO.getWeightKg());

        // 如果提供了体脂率，需要校验
        if (createDTO.getBodyFatPercentage() != null) {
            validateBodyFatPercentage(createDTO.getBodyFatPercentage());
        }

        // 如果提供了身高，需要校验
        if (createDTO.getHeightCm() != null) {
            validateHeightCm(createDTO.getHeightCm());
        }
    }

    /**
     * 校验更新DTO
     */
    private void validateUpdateDTO(WeightRecordUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了体重，需要校验
        if (updateDTO.getWeightKg() != null) {
            validateWeightKg(updateDTO.getWeightKg());
        }

        // 如果提供了体脂率，需要校验
        if (updateDTO.getBodyFatPercentage() != null) {
            validateBodyFatPercentage(updateDTO.getBodyFatPercentage());
        }

        // 如果提供了身高，需要校验
        if (updateDTO.getHeightCm() != null) {
            validateHeightCm(updateDTO.getHeightCm());
        }
    }

    /**
     * 校验体重
     */
    private void validateWeightKg(BigDecimal weightKg) {
        if (weightKg == null) {
            throw new BusinessException("400", "体重不能为空");
        }
        BigDecimal min = new BigDecimal("20.00");
        BigDecimal max = new BigDecimal("300.00");
        if (weightKg.compareTo(min) < 0 || weightKg.compareTo(max) > 0) {
            throw new BusinessException("400", "体重必须在20.00-300.00公斤之间");
        }
    }

    /**
     * 校验体脂率
     */
    private void validateBodyFatPercentage(BigDecimal bodyFatPercentage) {
        if (bodyFatPercentage == null) {
            return;
        }
        BigDecimal min = new BigDecimal("5.00");
        BigDecimal max = new BigDecimal("50.00");
        if (bodyFatPercentage.compareTo(min) < 0 || bodyFatPercentage.compareTo(max) > 0) {
            throw new BusinessException("400", "体脂率必须在5.00-50.00%之间");
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
     * 解析时间字符串（HH:mm格式）
     */
    private Date parseTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            LocalTime localTime = LocalTime.parse(timeStr.trim());
            // 使用今天的日期 + 解析的时间
            LocalDate today = LocalDate.now();
            return Date.from(localTime.atDate(today).atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            log.warn("解析时间失败: {}", timeStr, e);
            return null;
        }
    }

    /**
     * 转换为VO对象
     */
    private WeightRecordVO convertToVO(HealthWeightRecordPO po) {
        WeightRecordVO vo = new WeightRecordVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setRecordDate(po.getRecordDate());
        vo.setRecordTime(po.getRecordTime());
        vo.setWeightKg(po.getWeightKg());
        vo.setBodyFatPercentage(po.getBodyFatPercentage());
        vo.setMuscleMassKg(po.getMuscleMassKg());
        vo.setHeightCm(po.getHeightCm());
        vo.setBmi(po.getBmi());
        vo.setHealthStatus(po.getHealthStatus());
        vo.setNotes(po.getNotes());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

