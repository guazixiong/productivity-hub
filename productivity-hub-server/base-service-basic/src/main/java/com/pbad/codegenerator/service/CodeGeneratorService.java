package com.pbad.codegenerator.service;

import com.pbad.codegenerator.domain.dto.*;
import com.pbad.codegenerator.domain.vo.*;

import java.util.List;

/**
 * 代码生成器服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface CodeGeneratorService {

    // ========== 公司模板管理 ==========

    /**
     * 获取所有公司模板
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    List<CompanyTemplateVO> getAllCompanyTemplates(String userId);

    /**
     * 根据ID获取公司模板
     *
     * @param id 模板ID
     * @param userId 用户ID
     * @return 模板
     */
    CompanyTemplateVO getCompanyTemplateById(String id, String userId);

    /**
     * 创建或更新公司模板
     *
     * @param dto 模板DTO
     * @param userId 用户ID
     * @return 模板VO
     */
    CompanyTemplateVO saveCompanyTemplate(CompanyTemplateDTO dto, String userId);

    /**
     * 删除公司模板
     *
     * @param id 模板ID
     * @param userId 用户ID
     */
    void deleteCompanyTemplate(String id, String userId);

    // ========== 数据库配置管理 ==========

    /**
     * 获取所有数据库配置
     *
     * @param userId 用户ID
     * @return 配置列表
     */
    List<DatabaseConfigVO> getAllDatabaseConfigs(String userId);

    /**
     * 根据ID获取数据库配置
     *
     * @param id 配置ID
     * @param userId 用户ID
     * @return 配置
     */
    DatabaseConfigVO getDatabaseConfigById(String id, String userId);

    /**
     * 创建或更新数据库配置
     *
     * @param dto 配置DTO
     * @param userId 用户ID
     * @return 配置VO
     */
    DatabaseConfigVO saveDatabaseConfig(DatabaseConfigDTO dto, String userId);

    /**
     * 删除数据库配置
     *
     * @param id 配置ID
     * @param userId 用户ID
     */
    void deleteDatabaseConfig(String id, String userId);

    // ========== 表结构解析 ==========

    /**
     * 解析数据库表结构
     *
     * @param request 解析请求
     * @param userId 用户ID
     * @return 表信息列表
     */
    List<TableInfoVO> parseTableStructure(ParseTableRequestDTO request, String userId);

    // ========== 代码生成 ==========

    /**
     * 生成代码
     *
     * @param request 生成请求
     * @param userId 用户ID
     * @return 生成的代码列表
     */
    List<GeneratedCodeVO> generateCode(GenerateCodeRequestDTO request, String userId);

    /**
     * 生成代码并打包为zip
     *
     * @param request 生成请求
     * @param userId 用户ID
     * @return zip二进制
     */
    byte[] generateCodeZip(GenerateCodeRequestDTO request, String userId);
}

