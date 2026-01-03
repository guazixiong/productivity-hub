package com.pbad.health.service;

import com.pbad.health.domain.dto.WaterIntakeCreateDTO;
import com.pbad.health.domain.dto.WaterIntakeQueryDTO;
import com.pbad.health.domain.dto.WaterIntakeUpdateDTO;
import com.pbad.health.domain.vo.WaterIntakeVO;
import common.core.domain.PageResult;

import java.util.List;

/**
 * 饮水记录服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWaterIntakeService {

    /**
     * 创建饮水记录.
01
     *
     * @param createDTO 创建DTO
     * @param userId    用户ID
     * @return 饮水记录VO
     */
    WaterIntakeVO create(WaterIntakeCreateDTO createDTO, String userId);

    /**
     * 分页查询饮水记录列表.
02
     *
     * @param queryDTO 查询DTO
     * @param userId   用户ID
     * @return 分页结果
     */
    PageResult<WaterIntakeVO> queryPage(WaterIntakeQueryDTO queryDTO, String userId);

    /**
     * 根据ID查询饮水记录详情.
03
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 饮水记录VO
     */
    WaterIntakeVO getById(String id, String userId);

    /**
     * 更新饮水记录.
04
     *
     * @param id        记录ID
     * @param updateDTO 更新DTO
     * @param userId    用户ID
     * @return 饮水记录VO
     */
    WaterIntakeVO update(String id, WaterIntakeUpdateDTO updateDTO, String userId);

    /**
     * 删除饮水记录.
05
     *
     * @param id     记录ID
     * @param userId 用户ID
     */
    void delete(String id, String userId);

    /**
     * 批量删除饮水记录.
06
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     */
    void batchDelete(List<String> ids, String userId);
}

