package com.pbad.basic.codegenerator.controller;

import com.pbad.codegenerator.domain.dto.*;
import com.pbad.codegenerator.domain.vo.*;
import com.pbad.codegenerator.service.CodeGeneratorService;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 代码生成器控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/code-generator")
@RequiredArgsConstructor
public class CodeGeneratorController {

    private final CodeGeneratorService codeGeneratorService;

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        throw new BusinessException("401", "未登录或登录已过期");
    }

    // ========== 公司模板管理 ==========

    /**
     * 获取所有公司模板
     */
    @GetMapping("/templates")
    public ApiResponse<List<CompanyTemplateVO>> getAllCompanyTemplates() {
        String userId = getCurrentUserId();
        List<CompanyTemplateVO> templates = codeGeneratorService.getAllCompanyTemplates(userId);
        return ApiResponse.ok(templates);
    }

    /**
     * 根据ID获取公司模板
     */
    @GetMapping("/templates/{id}")
    public ApiResponse<CompanyTemplateVO> getCompanyTemplateById(@PathVariable String id) {
        String userId = getCurrentUserId();
        CompanyTemplateVO template = codeGeneratorService.getCompanyTemplateById(id, userId);
        return ApiResponse.ok(template);
    }

    /**
     * 创建或更新公司模板
     */
    @PostMapping("/templates")
    public ApiResponse<CompanyTemplateVO> saveCompanyTemplate(
            @RequestBody CompanyTemplateDTO dto) {
        String userId = getCurrentUserId();
        CompanyTemplateVO template = codeGeneratorService.saveCompanyTemplate(dto, userId);
        return ApiResponse.ok(template);
    }

    /**
     * 删除公司模板
     */
    @DeleteMapping("/templates/{id}")
    public ApiResponse<Void> deleteCompanyTemplate(@PathVariable String id) {
        String userId = getCurrentUserId();
        codeGeneratorService.deleteCompanyTemplate(id, userId);
        return ApiResponse.ok(null);
    }

    // ========== 数据库配置管理 ==========

    /**
     * 获取所有数据库配置
     */
    @GetMapping("/database-configs")
    public ApiResponse<List<DatabaseConfigVO>> getAllDatabaseConfigs() {
        String userId = getCurrentUserId();
        List<DatabaseConfigVO> configs = codeGeneratorService.getAllDatabaseConfigs(userId);
        return ApiResponse.ok(configs);
    }

    /**
     * 根据ID获取数据库配置
     */
    @GetMapping("/database-configs/{id}")
    public ApiResponse<DatabaseConfigVO> getDatabaseConfigById(@PathVariable String id) {
        String userId = getCurrentUserId();
        DatabaseConfigVO config = codeGeneratorService.getDatabaseConfigById(id, userId);
        return ApiResponse.ok(config);
    }

    /**
     * 创建或更新数据库配置
     */
    @PostMapping("/database-configs")
    public ApiResponse<DatabaseConfigVO> saveDatabaseConfig(
            @RequestBody DatabaseConfigDTO dto) {
        String userId = getCurrentUserId();
        DatabaseConfigVO config = codeGeneratorService.saveDatabaseConfig(dto, userId);
        return ApiResponse.ok(config);
    }

    /**
     * 删除数据库配置
     */
    @DeleteMapping("/database-configs/{id}")
    public ApiResponse<Void> deleteDatabaseConfig(@PathVariable String id) {
        String userId = getCurrentUserId();
        codeGeneratorService.deleteDatabaseConfig(id, userId);
        return ApiResponse.ok(null);
    }

    // ========== 表结构解析 ==========

    /**
     * 解析数据库表结构
     */
    @PostMapping("/parse-table")
    public ApiResponse<List<TableInfoVO>> parseTableStructure(@RequestBody ParseTableRequestDTO request) {
        String userId = getCurrentUserId();
        List<TableInfoVO> tables = codeGeneratorService.parseTableStructure(request, userId);
        return ApiResponse.ok(tables);
    }

    // ========== 代码生成 ==========

    /**
     * 生成代码
     */
    @PostMapping("/generate")
    public ApiResponse<List<GeneratedCodeVO>> generateCode(@RequestBody GenerateCodeRequestDTO request) {
        String userId = getCurrentUserId();
        List<GeneratedCodeVO> codes = codeGeneratorService.generateCode(request, userId);
        return ApiResponse.ok(codes);
    }

    /**
     * 生成代码并打包下载
     */
    @PostMapping(value = "/generate/zip", produces = "application/zip")
    public ResponseEntity<byte[]> generateCodeZip(@RequestBody GenerateCodeRequestDTO request) {
        String userId = getCurrentUserId();
        byte[] zipBytes = codeGeneratorService.generateCodeZip(request, userId);
        String fileName = Optional.ofNullable(request.getTableInfo())
                .map(TableInfoDTO::getName)
                .filter(org.springframework.util.StringUtils::hasText)
                .map(name -> name + "-code.zip")
                .orElse("code-generator.zip");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileName, java.nio.charset.StandardCharsets.UTF_8)
                .build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
    }
}

