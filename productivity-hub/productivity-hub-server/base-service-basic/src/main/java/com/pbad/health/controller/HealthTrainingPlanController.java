package com.pbad.health.controller;

import com.pbad.health.domain.dto.TrainingPlanCreateDTO;
import com.pbad.health.domain.dto.TrainingPlanQueryDTO;
import com.pbad.health.domain.dto.TrainingPlanUpdateDTO;
import com.pbad.health.domain.vo.TrainingPlanVO;
import com.pbad.health.service.HealthTrainingPlanService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 训练计划控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/exercise/plans")
@RequiredArgsConstructor
public class HealthTrainingPlanController extends BaseHealthController {

    private final HealthTrainingPlanService trainingPlanService;

    /**
     * 查询训练计划列表.
02
     *
     * @param queryDTO 查询参数
     * @return 训练计划列表
     */
    @GetMapping
    public ApiResponse<List<TrainingPlanVO>> queryList(TrainingPlanQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            List<TrainingPlanVO> list = trainingPlanService.queryList(queryDTO, userId);
            return ApiResponse.ok(list);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询训练计划列表失败", e);
            return ApiResponse.fail("查询训练计划列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询训练计划详情.
03
     *
     * @param id 计划ID
     * @return 训练计划详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TrainingPlanVO> getById(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.getById(id, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询训练计划详情失败，ID: {}", id, e);
            return ApiResponse.fail("查询训练计划详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建训练计划.
01
     *
     * @param createDTO 创建请求体
     * @return 训练计划详情
     */
    @PostMapping
    public ApiResponse<TrainingPlanVO> create(@RequestBody TrainingPlanCreateDTO createDTO) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.create(createDTO, userId);
            return ApiResponse.ok("创建成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建训练计划失败", e);
            return ApiResponse.fail("创建训练计划失败：" + e.getMessage());
        }
    }

    /**
     * 更新训练计划.
04
     *
     * @param id        计划ID
     * @param updateDTO 更新请求体
     * @return 训练计划详情
     */
    @PutMapping("/{id}")
    public ApiResponse<TrainingPlanVO> update(
            @PathVariable String id,
            @RequestBody TrainingPlanUpdateDTO updateDTO) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.update(id, updateDTO, userId);
            return ApiResponse.ok("更新成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新训练计划失败，ID: {}", id, e);
            return ApiResponse.fail("更新训练计划失败：" + e.getMessage());
        }
    }

    /**
     * 暂停训练计划.
05
     *
     * @param id 计划ID
     * @return 训练计划详情
     */
    @PostMapping("/{id}/pause")
    public ApiResponse<TrainingPlanVO> pause(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.pause(id, userId);
            return ApiResponse.ok("暂停成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("暂停训练计划失败，ID: {}", id, e);
            return ApiResponse.fail("暂停训练计划失败：" + e.getMessage());
        }
    }

    /**
     * 恢复训练计划.
06
     *
     * @param id 计划ID
     * @return 训练计划详情
     */
    @PostMapping("/{id}/resume")
    public ApiResponse<TrainingPlanVO> resume(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.resume(id, userId);
            return ApiResponse.ok("恢复成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("恢复训练计划失败，ID: {}", id, e);
            return ApiResponse.fail("恢复训练计划失败：" + e.getMessage());
        }
    }

    /**
     * 完成训练计划.
07
     *
     * @param id 计划ID
     * @return 训练计划详情
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<TrainingPlanVO> complete(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            TrainingPlanVO vo = trainingPlanService.complete(id, userId);
            return ApiResponse.ok("完成成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("完成训练计划失败，ID: {}", id, e);
            return ApiResponse.fail("完成训练计划失败：" + e.getMessage());
        }
    }

    /**
     * 删除训练计划.
08
     *
     * @param id 计划ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            trainingPlanService.delete(id, userId);
            return ApiResponse.ok("删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除训练计划失败，ID: {}", id, e);
            return ApiResponse.fail("删除训练计划失败：" + e.getMessage());
        }
    }
}

