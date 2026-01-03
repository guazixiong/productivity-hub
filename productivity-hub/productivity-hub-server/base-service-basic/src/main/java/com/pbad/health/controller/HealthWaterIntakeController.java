package com.pbad.health.controller;

import com.pbad.health.domain.dto.WaterIntakeCreateDTO;
import com.pbad.health.domain.dto.WaterIntakeQueryDTO;
import com.pbad.health.domain.dto.WaterIntakeUpdateDTO;
import com.pbad.health.domain.vo.WaterIntakeVO;
import com.pbad.health.service.HealthWaterIntakeExportService;
import com.pbad.health.service.HealthWaterIntakeService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 饮水记录控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/water/records")
@RequiredArgsConstructor
public class HealthWaterIntakeController extends BaseHealthController {

    private final HealthWaterIntakeService waterIntakeService;
    private final HealthWaterIntakeExportService exportService;

    /**
     * 查询饮水记录列表（分页）.
02
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<WaterIntakeVO>> queryPage(WaterIntakeQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            PageResult<WaterIntakeVO> result = waterIntakeService.queryPage(queryDTO, userId);
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水记录列表失败", e);
            return ApiResponse.fail("查询饮水记录列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询饮水记录详情.
03
     *
     * @param id 记录ID
     * @return 饮水记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WaterIntakeVO> getById(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            WaterIntakeVO vo = waterIntakeService.getById(id, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询饮水记录详情失败，ID: {}", id, e);
            return ApiResponse.fail("查询饮水记录详情失败：" + e.getMessage());
        }
    }

    /**
     * 新增饮水记录.
01
     *
     * @param createDTO 创建请求体
     * @return 饮水记录详情
     */
    @PostMapping
    public ApiResponse<WaterIntakeVO> create(@RequestBody WaterIntakeCreateDTO createDTO) {
        try {
            String userId = getCurrentUserId();
            WaterIntakeVO vo = waterIntakeService.create(createDTO, userId);
            return ApiResponse.ok("创建成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建饮水记录失败", e);
            return ApiResponse.fail("创建饮水记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新饮水记录.
04
     *
     * @param id        记录ID
     * @param updateDTO 更新请求体
     * @return 饮水记录详情
     */
    @PutMapping("/{id}")
    public ApiResponse<WaterIntakeVO> update(
            @PathVariable String id,
            @RequestBody WaterIntakeUpdateDTO updateDTO) {
        try {
            String userId = getCurrentUserId();
            WaterIntakeVO vo = waterIntakeService.update(id, updateDTO, userId);
            return ApiResponse.ok("更新成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新饮水记录失败，ID: {}", id, e);
            return ApiResponse.fail("更新饮水记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除饮水记录.
05
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            waterIntakeService.delete(id, userId);
            return ApiResponse.ok("删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除饮水记录失败，ID: {}", id, e);
            return ApiResponse.fail("删除饮水记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除饮水记录.
06
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<String> ids) {
        try {
            String userId = getCurrentUserId();
            waterIntakeService.batchDelete(ids, userId);
            return ApiResponse.ok("批量删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除饮水记录失败", e);
            return ApiResponse.fail("批量删除饮水记录失败：" + e.getMessage());
        }
    }

    /**
     * 导出饮水记录（Excel格式）.
07
     *
     * @param queryDTO 查询参数
     * @return Excel文件
     */
    @GetMapping("/export/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportToExcel(WaterIntakeQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] excelData = exportService.exportToExcel(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "饮水记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".xlsx";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("导出饮水记录Excel失败", e);
            throw new BusinessException("500", "导出Excel失败：" + e.getMessage());
        }
    }

    /**
     * 导出饮水记录（CSV格式）.
08
     *
     * @param queryDTO 查询参数
     * @return CSV文件
     */
    @GetMapping("/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportToCsv(WaterIntakeQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] csvData = exportService.exportToCsv(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "饮水记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".csv";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            log.error("导出饮水记录CSV失败", e);
            throw new BusinessException("500", "导出CSV失败：" + e.getMessage());
        }
    }
}

