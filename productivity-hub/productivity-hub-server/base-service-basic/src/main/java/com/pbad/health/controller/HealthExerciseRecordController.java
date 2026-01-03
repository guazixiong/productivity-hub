package com.pbad.health.controller;

import com.pbad.health.domain.dto.ExerciseRecordCreateDTO;
import com.pbad.health.domain.dto.ExerciseRecordQueryDTO;
import com.pbad.health.domain.dto.ExerciseRecordUpdateDTO;
import com.pbad.health.domain.dto.ExerciseStatisticsQueryDTO;
import com.pbad.health.domain.dto.ExerciseTrendQueryDTO;
import com.pbad.health.domain.vo.ExerciseRecordImportResultVO;
import com.pbad.health.domain.vo.ExerciseRecordVO;
import com.pbad.health.domain.vo.ExerciseStatisticsVO;
import com.pbad.health.domain.vo.ExerciseTrendVO;
import com.pbad.health.service.HealthExerciseRecordExportService;
import com.pbad.health.service.HealthExerciseRecordImportService;
import com.pbad.health.service.HealthExerciseRecordService;
import com.pbad.health.service.HealthExerciseStatisticsService;
import common.core.domain.ApiResponse;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 运动记录控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/exercise/records")
@RequiredArgsConstructor
public class HealthExerciseRecordController extends BaseHealthController {

    private final HealthExerciseRecordService exerciseRecordService;
    private final HealthExerciseStatisticsService statisticsService;
    private final HealthExerciseRecordExportService exportService;
    private final HealthExerciseRecordImportService importService;

    /**
     * 查询运动记录列表（分页）.
001-02
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<ExerciseRecordVO>> queryPage(ExerciseRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            PageResult<ExerciseRecordVO> result = exerciseRecordService.queryPage(queryDTO, userId);
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询运动记录列表失败", e);
            return ApiResponse.fail("查询运动记录列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询运动记录详情.
001-03
     *
     * @param id 记录ID
     * @return 运动记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ExerciseRecordVO> getById(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            ExerciseRecordVO vo = exerciseRecordService.getById(id, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询运动记录详情失败，ID: {}", id, e);
            return ApiResponse.fail("查询运动记录详情失败：" + e.getMessage());
        }
    }

    /**
     * 新增运动记录.
001-01
     *
     * @param createDTO 创建请求体
     * @return 运动记录详情
     */
    @PostMapping
    public ApiResponse<ExerciseRecordVO> create(@RequestBody ExerciseRecordCreateDTO createDTO) {
        try {
            String userId = getCurrentUserId();
            ExerciseRecordVO vo = exerciseRecordService.create(createDTO, userId);
            return ApiResponse.ok("创建成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建运动记录失败", e);
            return ApiResponse.fail("创建运动记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新运动记录.
001-04
     *
     * @param id        记录ID
     * @param updateDTO 更新请求体
     * @return 运动记录详情
     */
    @PutMapping("/{id}")
    public ApiResponse<ExerciseRecordVO> update(
            @PathVariable String id,
            @RequestBody ExerciseRecordUpdateDTO updateDTO) {
        try {
            String userId = getCurrentUserId();
            ExerciseRecordVO vo = exerciseRecordService.update(id, updateDTO, userId);
            return ApiResponse.ok("更新成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新运动记录失败，ID: {}", id, e);
            return ApiResponse.fail("更新运动记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除运动记录.
001-05
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            exerciseRecordService.delete(id, userId);
            return ApiResponse.ok("删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除运动记录失败，ID: {}", id, e);
            return ApiResponse.fail("删除运动记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除运动记录.
001-06
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<String> ids) {
        try {
            String userId = getCurrentUserId();
            exerciseRecordService.batchDelete(ids, userId);
            return ApiResponse.ok("批量删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除运动记录失败", e);
            return ApiResponse.fail("批量删除运动记录失败：" + e.getMessage());
        }
    }

    /**
     * 查询运动统计数据.
002-01
     *
     * @param queryDTO 查询参数
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<ExerciseStatisticsVO> getStatistics(ExerciseStatisticsQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            ExerciseStatisticsVO vo = statisticsService.getStatistics(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询运动统计数据失败", e);
            return ApiResponse.fail("查询运动统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 查询运动趋势数据.
002-02
     *
     * @param queryDTO 查询参数
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public ApiResponse<ExerciseTrendVO> getTrend(ExerciseTrendQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            ExerciseTrendVO vo = statisticsService.getTrend(queryDTO, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询运动趋势数据失败", e);
            return ApiResponse.fail("查询运动趋势数据失败：" + e.getMessage());
        }
    }

    /**
     * 导出运动记录（Excel格式）.
001-07
     *
     * @param queryDTO 查询参数
     * @return Excel文件
     */
    @GetMapping("/export/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportToExcel(ExerciseRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] excelData = exportService.exportToExcel(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "运动记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".xlsx";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("导出运动记录Excel失败", e);
            throw new BusinessException("500", "导出Excel失败：" + e.getMessage());
        }
    }

    /**
     * 导出运动记录（CSV格式）.
001-08
     *
     * @param queryDTO 查询参数
     * @return CSV文件
     */
    @GetMapping("/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportToCsv(ExerciseRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] csvData = exportService.exportToCsv(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "运动记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".csv";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            log.error("导出运动记录CSV失败", e);
            throw new BusinessException("500", "导出CSV失败：" + e.getMessage());
        }
    }

    /**
     * 导入运动记录（Excel格式）.
001-09
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ApiResponse<ExerciseRecordImportResultVO> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            ExerciseRecordImportResultVO result = importService.importFromExcel(file);
            return ApiResponse.ok("导入完成", result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导入运动记录失败", e);
            return ApiResponse.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 下载运动记录导入模板.
001-10
     *
     * @return Excel模板文件
     */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = importService.downloadTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "运动记录导入模板.xlsx";
            headers.setContentDispositionFormData("attachment", 
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (Exception e) {
            log.error("下载运动记录导入模板失败", e);
            throw new BusinessException("500", "下载模板失败：" + e.getMessage());
        }
    }
}

