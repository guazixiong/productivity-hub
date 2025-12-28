package com.pbad.health.controller;

import com.pbad.health.domain.dto.UserBodyInfoCreateOrUpdateDTO;
import com.pbad.health.domain.vo.UserBodyInfoVO;
import com.pbad.health.service.HealthUserBodyInfoService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户身体信息控制器.
 * 关联需求：REQ-HEALTH-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/weight/body-info")
@RequiredArgsConstructor
public class HealthUserBodyInfoController extends BaseHealthController {

    private final HealthUserBodyInfoService userBodyInfoService;

    /**
     * 查询用户身体信息.
     * 关联接口：API-REQ-HEALTH-006-02
     *
     * @return 用户身体信息VO（如果不存在，返回null）
     */
    @GetMapping
    public ApiResponse<UserBodyInfoVO> getBodyInfo() {
        try {
            String userId = getCurrentUserId();
            UserBodyInfoVO vo = userBodyInfoService.getByUserId(userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询用户身体信息失败", e);
            return ApiResponse.fail("查询用户身体信息失败：" + e.getMessage());
        }
    }

    /**
     * 设置/更新用户身体信息（UPSERT）.
     * 关联接口：API-REQ-HEALTH-006-01
     *
     * @param dto 创建或更新DTO
     * @return 用户身体信息VO
     */
    @PostMapping
    public ApiResponse<UserBodyInfoVO> createOrUpdate(@RequestBody UserBodyInfoCreateOrUpdateDTO dto) {
        try {
            String userId = getCurrentUserId();
            UserBodyInfoVO vo = userBodyInfoService.createOrUpdate(dto, userId);
            return ApiResponse.ok("设置成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("设置用户身体信息失败", e);
            return ApiResponse.fail("设置用户身体信息失败：" + e.getMessage());
        }
    }
}

