package com.pbad.asset.controller;

import com.pbad.asset.domain.vo.DataImportResultVO;
import com.pbad.asset.service.DataManagementService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 数据管理控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataManagementController {

    private final DataManagementService dataManagementService;

    /**
     * 导入数据
     */
    @PostMapping("/import")
    public ApiResponse<DataImportResultVO> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dataType", defaultValue = "asset") String dataType,
            @RequestParam(value = "incremental", defaultValue = "false") boolean incremental) {
        DataImportResultVO result = dataManagementService.importData(file, dataType, incremental);
        return ApiResponse.ok(result);
    }

    /**
     * 导出数据
     */
    @GetMapping("/export")
    public void exportData(
            @RequestParam(value = "dataType", defaultValue = "asset") String dataType,
            @RequestParam(value = "format", defaultValue = "excel") String format,
            HttpServletResponse response) throws IOException {
        dataManagementService.exportData(dataType, format, response);
    }
}

