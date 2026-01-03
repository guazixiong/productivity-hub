package com.pbad.health.controller;

import com.pbad.health.domain.dto.WaterTargetCreateOrUpdateDTO;
import com.pbad.health.domain.vo.WaterTargetProgressVO;
import com.pbad.health.domain.vo.WaterTargetVO;
import com.pbad.health.service.HealthWaterTargetService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 饮水目标控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/water")
@RequiredArgsConstructor
public class HealthWaterTargetController extends BaseHealthController {

    private final HealthWaterTargetService waterTargetService;

    /**
     * 查询饮水目标配置.
02
     *
     * @return 饮水目标VO（如果不存在，返回默认值）
     */
    @GetMapping("/target")
    public ApiResponse<WaterTargetVO> getTarget() {
        try {
            String userId = getCurrentUserId();
            WaterTargetVO vo = waterTargetService.getByUserId(userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水目标失败", e);
            return ApiResponse.fail("查询饮水目标失败：" + e.getMessage());
        }
    }

    /**
     * 设置/更新饮水目标（UPSERT）.
01
     *
     * @param dto 创建或更新DTO
     * @return 饮水目标VO
     */
    @PostMapping("/target")
    public ApiResponse<WaterTargetVO> createOrUpdate(@RequestBody WaterTargetCreateOrUpdateDTO dto) {
        try {
            String userId = getCurrentUserId();
            WaterTargetVO vo = waterTargetService.createOrUpdate(dto, userId);
            return ApiResponse.ok("设置成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("设置饮水目标失败", e);
            return ApiResponse.fail("设置饮水目标失败：" + e.getMessage());
        }
    }

    /**
     * 查询当日饮水进度.
03
     *
     * @param date 查询日期（可选，格式：yyyy-MM-dd，默认当天）
     * @return 饮水进度VO
     */
    @GetMapping("/progress")
    public ApiResponse<WaterTargetProgressVO> getProgress(@RequestParam(required = false) String date) {
        try {
            String userId = getCurrentUserId();
            WaterTargetProgressVO vo = waterTargetService.getProgress(userId, date);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水进度失败", e);
            return ApiResponse.fail("查询饮水进度失败：" + e.getMessage());
        }
    }
}

