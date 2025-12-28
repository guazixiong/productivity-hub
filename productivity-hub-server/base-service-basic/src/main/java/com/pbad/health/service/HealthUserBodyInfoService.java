package com.pbad.health.service;

import com.pbad.health.domain.dto.UserBodyInfoCreateOrUpdateDTO;
import com.pbad.health.domain.vo.UserBodyInfoVO;

/**
 * 用户身体信息服务接口.
 * 关联需求：REQ-HEALTH-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface HealthUserBodyInfoService {

    /**
     * 创建或更新用户身体信息（UPSERT）.
     * 关联接口：API-REQ-HEALTH-006-01
     *
     * @param dto    创建或更新DTO
     * @param userId 用户ID
     * @return 用户身体信息VO
     */
    UserBodyInfoVO createOrUpdate(UserBodyInfoCreateOrUpdateDTO dto, String userId);

    /**
     * 查询用户身体信息.
     * 关联接口：API-REQ-HEALTH-006-02
     *
     * @param userId 用户ID
     * @return 用户身体信息VO（如果不存在，返回null）
     */
    UserBodyInfoVO getByUserId(String userId);
}

