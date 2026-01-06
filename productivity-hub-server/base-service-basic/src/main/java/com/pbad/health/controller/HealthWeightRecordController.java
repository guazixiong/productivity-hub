package com.pbad.health.controller;

import com.pbad.health.domain.dto.WeightRecordCreateDTO;
import com.pbad.health.domain.dto.WeightRecordQueryDTO;
import com.pbad.health.domain.dto.WeightRecordUpdateDTO;
import com.pbad.health.domain.vo.WeightRecordVO;
import com.pbad.health.domain.vo.WeightRecordImportResultVO;
import com.pbad.health.service.HealthWeightRecordExportService;
import com.pbad.health.service.HealthWeightRecordImportService;
import com.pbad.health.service.HealthWeightRecordService;
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
 * 体重记录控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/health/weight/records")
@RequiredArgsConstructor
public class HealthWeightRecordController extends BaseHealthController {

    private final HealthWeightRecordService weightRecordService;
    private final HealthWeightRecordExportService exportService;
    private final HealthWeightRecordImportService importService;

    /**
     * 查询体重记录列表（分页）.
02
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<WeightRecordVO>> queryPage(WeightRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            PageResult<WeightRecordVO> result = weightRecordService.queryPage(queryDTO, userId);
            return ApiResponse.ok(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询体重记录列表失败", e);
            return ApiResponse.fail("查询体重记录列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询体重记录详情.
03
     *
     * @param id 记录ID
     * @return 体重记录详情
     */
    @GetMapping("/{id}")
    public ApiResponse<WeightRecordVO> getById(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            WeightRecordVO vo = weightRecordService.getById(id, userId);
            return ApiResponse.ok(vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询体重记录详情失败，ID: {}", id, e);
            return ApiResponse.fail("查询体重记录详情失败：" + e.getMessage());
        }
    }

    /**
     * 新增体重记录.
01
     *
     * @param createDTO 创建请求体
     * @return 体重记录详情
     */
    @PostMapping
    public ApiResponse<WeightRecordVO> create(@RequestBody WeightRecordCreateDTO createDTO) {
        try {
            String userId = getCurrentUserId();
            WeightRecordVO vo = weightRecordService.create(createDTO, userId);
            return ApiResponse.ok("创建成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建体重记录失败", e);
            return ApiResponse.fail("创建体重记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新体重记录.
04
     *
     * @param id        记录ID
     * @param updateDTO 更新请求体
     * @return 体重记录详情
     */
    @PutMapping("/{id}")
    public ApiResponse<WeightRecordVO> update(
            @PathVariable String id,
            @RequestBody WeightRecordUpdateDTO updateDTO) {
        try {
            String userId = getCurrentUserId();
            WeightRecordVO vo = weightRecordService.update(id, updateDTO, userId);
            return ApiResponse.ok("更新成功", vo);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新体重记录失败，ID: {}", id, e);
            return ApiResponse.fail("更新体重记录失败：" + e.getMessage());
        }
    }

    /**
     * 删除体重记录.
05
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            weightRecordService.delete(id, userId);
            return ApiResponse.ok("删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除体重记录失败，ID: {}", id, e);
            return ApiResponse.fail("删除体重记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除体重记录.
06
     *
     * @param ids 记录ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDelete(@RequestBody List<String> ids) {
        try {
            String userId = getCurrentUserId();
            weightRecordService.batchDelete(ids, userId);
            return ApiResponse.ok("批量删除成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除体重记录失败", e);
            return ApiResponse.fail("批量删除体重记录失败：" + e.getMessage());
        }
    }

    /**
     * 导出体重记录（Excel格式）.
07
     *
     * @param queryDTO 查询参数
     * @return Excel文件
     */
    @GetMapping("/export/excel")
    public org.springframework.http.ResponseEntity<byte[]> exportToExcel(WeightRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] excelData = exportService.exportToExcel(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "体重记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".xlsx";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            log.error("导出体重记录Excel失败", e);
            throw new BusinessException("500", "导出Excel失败：" + e.getMessage());
        }
    }

    /**
     * 导出体重记录（CSV格式）.
08
     *
     * @param queryDTO 查询参数
     * @return CSV文件
     */
    @GetMapping("/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportToCsv(WeightRecordQueryDTO queryDTO) {
        try {
            String userId = getCurrentUserId();
            byte[] csvData = exportService.exportToCsv(queryDTO, userId);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "体重记录_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".csv";
            headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);
        } catch (Exception e) {
            log.error("导出体重记录CSV失败", e);
            throw new BusinessException("500", "导出CSV失败：" + e.getMessage());
        }
    }

    /**
     * 导入体重记录（Excel格式）.
09
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ApiResponse<WeightRecordImportResultVO> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            WeightRecordImportResultVO result = importService.importFromExcel(file);
            return ApiResponse.ok("导入完成", result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导入体重记录失败", e);
            return ApiResponse.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 下载体重记录导入模板.
10
     *
     * @return Excel模板文件
     */
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = importService.downloadTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "体重记录导入模板.xlsx";
            headers.setContentDispositionFormData("attachment", 
                new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (Exception e) {
            log.error("下载体重记录导入模板失败", e);
            throw new BusinessException("500", "下载模板失败：" + e.getMessage());
        }
    }
}

