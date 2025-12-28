package com.pbad.health.service;

import com.pbad.health.domain.dto.WaterTargetCreateOrUpdateDTO;
import com.pbad.health.domain.vo.WaterTargetProgressVO;
import com.pbad.health.domain.vo.WaterTargetVO;

/**
 * 饮水目标服务接口.
 * 关联需求：REQ-HEALTH-004
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthWaterTargetService {

    /**
     * 创建或更新饮水目标（UPSERT）.
     * 关联接口：API-REQ-HEALTH-004-01
     *
     * @param dto   创建或更新DTO
     * @param userId 用户ID
     * @return 饮水目标VO
     */
    WaterTargetVO createOrUpdate(WaterTargetCreateOrUpdateDTO dto, String userId);

    /**
     * 查询饮水目标配置.
     * 关联接口：API-REQ-HEALTH-004-02
     *
     * @param userId 用户ID
     * @return 饮水目标VO（如果不存在，返回默认值）
     */
    WaterTargetVO getByUserId(String userId);

    /**
     * 查询当日饮水进度.
     * 关联接口：API-REQ-HEALTH-004-03
     *
     * @param userId    用户ID
     * @param queryDate 查询日期（可选，格式：yyyy-MM-dd，默认当天）
     * @return 饮水进度VO
     */
    WaterTargetProgressVO getProgress(String userId, String queryDate);
}

