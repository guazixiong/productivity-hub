package com.pbad.health.service.impl;

import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.health.domain.dto.WaterIntakeCreateDTO;
import com.pbad.health.domain.dto.WaterIntakeQueryDTO;
import com.pbad.health.domain.dto.WaterIntakeUpdateDTO;
import com.pbad.health.domain.po.HealthWaterIntakePO;
import com.pbad.health.domain.vo.WaterIntakeVO;
import com.pbad.health.mapper.HealthWaterIntakeMapper;
import com.pbad.health.service.HealthWaterIntakeService;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 饮水记录服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthWaterIntakeServiceImpl implements HealthWaterIntakeService {

    private final HealthWaterIntakeMapper waterIntakeMapper;
    private final IdGeneratorApi idGeneratorApi;

    // 饮水类型枚举值
    private static final String[] WATER_TYPES = {
            "白开水", "矿泉水", "纯净水", "茶水", "咖啡", "果汁", "运动饮料", "其他"
    };

    // 排序字段白名单
    private static final String[] SORT_FIELDS = {
            "intakeDate", "intakeTime", "volumeMl"
    };

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaterIntakeVO create(WaterIntakeCreateDTO createDTO, String userId) {
        // 参数校验
        validateCreateDTO(createDTO);

        // 生成ID
        String id = idGeneratorApi.generateId();

        // 构建PO对象
        HealthWaterIntakePO po = new HealthWaterIntakePO();
        po.setId(id);
        po.setUserId(userId);

        // 处理饮水日期（默认当天）
        if (StringUtils.hasText(createDTO.getIntakeDate())) {
            Date intakeDate = parseDate(createDTO.getIntakeDate());
            if (intakeDate == null) {
                throw new BusinessException("400", "饮水日期格式错误，应为yyyy-MM-dd");
            }
            po.setIntakeDate(intakeDate);
        } else {
            // 默认当天
            po.setIntakeDate(new Date());
        }

        // 处理饮水时间（默认当前时间）
        if (StringUtils.hasText(createDTO.getIntakeTime())) {
            Date intakeTime = parseTime(createDTO.getIntakeTime());
            if (intakeTime == null) {
                throw new BusinessException("400", "饮水时间格式错误，应为HH:mm");
            }
            po.setIntakeTime(intakeTime);
        } else {
            // 默认当前时间
            po.setIntakeTime(new Date());
        }

        po.setWaterType(createDTO.getWaterType());
        po.setVolumeMl(createDTO.getVolumeMl());
        po.setNotes(createDTO.getNotes());

        // 设置创建时间和更新时间
        Date now = new Date();
        po.setCreatedAt(now);
        po.setUpdatedAt(now);

        // 插入数据库
        int result = waterIntakeMapper.insert(po);
        if (result <= 0) {
            throw new BusinessException("500", "创建饮水记录失败");
        }

        log.info("用户{}创建饮水记录成功，ID: {}", userId, id);

        // 转换为VO并返回
        return convertToVO(po);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<WaterIntakeVO> queryPage(WaterIntakeQueryDTO queryDTO, String userId) {
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
            sortField = "intakeDate";
        } else {
            boolean valid = false;
            for (String field : SORT_FIELDS) {
                if (field.equals(sortField)) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                sortField = "intakeDate";
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
        List<HealthWaterIntakePO> poList = waterIntakeMapper.selectByPage(
                userId,
                queryDTO.getWaterTypes(),
                startDate,
                endDate,
                sortField,
                sortOrder,
                offset,
                pageSize
        );

        // 统计总数
        long total = waterIntakeMapper.countByCondition(
                userId,
                queryDTO.getWaterTypes(),
                startDate,
                endDate
        );

        // 转换为VO列表
        List<WaterIntakeVO> voList = poList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<WaterIntakeVO> pageResult = new PageResult<>();
        pageResult.setPageNum(pageNum);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setItems(voList);

        return pageResult;
    }

    @Override
    @Transactional(readOnly = true)
    public WaterIntakeVO getById(String id, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        HealthWaterIntakePO po = waterIntakeMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "饮水记录不存在");
        }

        return convertToVO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WaterIntakeVO update(String id, WaterIntakeUpdateDTO updateDTO, String userId) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException("400", "记录ID不能为空");
        }

        // 查询原记录（验证数据归属）
        HealthWaterIntakePO existingPo = waterIntakeMapper.selectById(id, userId);
        if (existingPo == null) {
            throw new BusinessException("404", "饮水记录不存在");
        }

        // 参数校验
        validateUpdateDTO(updateDTO);

        // 更新字段
        boolean hasUpdate = false;

        if (StringUtils.hasText(updateDTO.getIntakeDate())) {
            Date intakeDate = parseDate(updateDTO.getIntakeDate());
            if (intakeDate == null) {
                throw new BusinessException("400", "饮水日期格式错误，应为yyyy-MM-dd");
            }
            existingPo.setIntakeDate(intakeDate);
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getIntakeTime())) {
            Date intakeTime = parseTime(updateDTO.getIntakeTime());
            if (intakeTime == null) {
                throw new BusinessException("400", "饮水时间格式错误，应为HH:mm");
            }
            existingPo.setIntakeTime(intakeTime);
            hasUpdate = true;
        }

        if (StringUtils.hasText(updateDTO.getWaterType())) {
            validateWaterType(updateDTO.getWaterType());
            existingPo.setWaterType(updateDTO.getWaterType());
            hasUpdate = true;
        }

        if (updateDTO.getVolumeMl() != null) {
            validateVolumeMl(updateDTO.getVolumeMl());
            existingPo.setVolumeMl(updateDTO.getVolumeMl());
            hasUpdate = true;
        }

        if (updateDTO.getNotes() != null) {
            existingPo.setNotes(updateDTO.getNotes());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BusinessException("400", "没有需要更新的字段");
        }

        // 设置更新时间
        existingPo.setUpdatedAt(new Date());

        // 更新数据库
        int result = waterIntakeMapper.update(existingPo);
        if (result <= 0) {
            throw new BusinessException("500", "更新饮水记录失败");
        }

        log.info("用户{}更新饮水记录成功，ID: {}", userId, id);

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
        HealthWaterIntakePO po = waterIntakeMapper.selectById(id, userId);
        if (po == null) {
            throw new BusinessException("404", "饮水记录不存在");
        }

        // 删除记录
        int result = waterIntakeMapper.deleteById(id, userId);
        if (result <= 0) {
            throw new BusinessException("500", "删除饮水记录失败");
        }

        log.info("用户{}删除饮水记录成功，ID: {}", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<String> ids, String userId) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException("400", "记录ID列表不能为空");
        }

        // 批量删除
        int result = waterIntakeMapper.batchDeleteByIds(ids, userId);
        if (result <= 0) {
            throw new BusinessException("500", "批量删除饮水记录失败");
        }

        log.info("用户{}批量删除饮水记录成功，删除数量: {}", userId, result);
    }

    /**
     * 校验创建DTO
     */
    private void validateCreateDTO(WaterIntakeCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 校验饮水类型（必填）
        if (!StringUtils.hasText(createDTO.getWaterType())) {
            throw new BusinessException("400", "饮水类型不能为空");
        }
        validateWaterType(createDTO.getWaterType());

        // 校验饮水量（必填）
        if (createDTO.getVolumeMl() == null) {
            throw new BusinessException("400", "饮水量不能为空");
        }
        validateVolumeMl(createDTO.getVolumeMl());
    }

    /**
     * 校验更新DTO
     */
    private void validateUpdateDTO(WaterIntakeUpdateDTO updateDTO) {
        if (updateDTO == null) {
            throw new BusinessException("400", "请求参数不能为空");
        }

        // 如果提供了饮水类型，需要校验
        if (StringUtils.hasText(updateDTO.getWaterType())) {
            validateWaterType(updateDTO.getWaterType());
        }

        // 如果提供了饮水量，需要校验
        if (updateDTO.getVolumeMl() != null) {
            validateVolumeMl(updateDTO.getVolumeMl());
        }
    }

    /**
     * 校验饮水类型
     */
    private void validateWaterType(String waterType) {
        boolean valid = false;
        for (String type : WATER_TYPES) {
            if (type.equals(waterType)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException("400", "饮水类型无效，可选值：白开水、矿泉水、纯净水、茶水、咖啡、果汁、运动饮料、其他");
        }
    }

    /**
     * 校验饮水量
     */
    private void validateVolumeMl(Integer volumeMl) {
        if (volumeMl == null || volumeMl < 1 || volumeMl > 5000) {
            throw new BusinessException("400", "饮水量必须在1-5000毫升之间");
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
    private WaterIntakeVO convertToVO(HealthWaterIntakePO po) {
        WaterIntakeVO vo = new WaterIntakeVO();
        vo.setId(po.getId());
        vo.setUserId(po.getUserId());
        vo.setIntakeDate(po.getIntakeDate());
        vo.setIntakeTime(po.getIntakeTime());
        vo.setWaterType(po.getWaterType());
        vo.setVolumeMl(po.getVolumeMl());
        vo.setNotes(po.getNotes());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

