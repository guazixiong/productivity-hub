package com.pbad.health.service;

import com.pbad.health.domain.dto.WeightRecordCreateDTO;
import com.pbad.health.domain.dto.WeightRecordQueryDTO;
import com.pbad.health.domain.dto.WeightRecordUpdateDTO;
import com.pbad.health.domain.vo.WeightRecordVO;
import common.core.domain.PageResult;

import java.util.List;

/**
 * 体重记录服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWeightRecordService {

    /**
     * 创建体重记录.
01
     *
     * @param createDTO 创建DTO
     * @param userId    用户ID
     * @return 体重记录VO
     */
    WeightRecordVO create(WeightRecordCreateDTO createDTO, String userId);

    /**
     * 分页查询体重记录列表.
02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 分页结果
     */
    PageResult<WeightRecordVO> queryPage(WeightRecordQueryDTO queryDTO, String userId);

    /**
     * 根据ID查询体重记录详情.
03
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 体重记录VO
     */
    WeightRecordVO getById(String id, String userId);

    /**
     * 更新体重记录.
04
     *
     * @param id        记录ID
     * @param updateDTO 更新DTO
     * @param userId    用户ID
     * @return 体重记录VO
     */
    WeightRecordVO update(String id, WeightRecordUpdateDTO updateDTO, String userId);

    /**
     * 删除体重记录.
05
     *
     * @param id     记录ID
     * @param userId 用户ID
     */
    void delete(String id, String userId);

    /**
     * 批量删除体重记录.
06
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     */
    void batchDelete(List<String> ids, String userId);
}

