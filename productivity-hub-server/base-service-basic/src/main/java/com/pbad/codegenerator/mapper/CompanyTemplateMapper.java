package com.pbad.codegenerator.mapper;

import com.pbad.codegenerator.domain.po.CompanyTemplatePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司模板 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface CompanyTemplateMapper {

    /**
     * 查询所有模板
     *
     * @return 模板列表
     */
    List<CompanyTemplatePO> selectAll();

    /**
     * 根据用户ID查询模板列表
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    List<CompanyTemplatePO> selectByUserId(@Param("userId") String userId);

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板
     */
    CompanyTemplatePO selectById(@Param("id") String id);

    /**
     * 根据ID和用户ID查询模板（用于权限验证）
     *
     * @param id 模板ID
     * @param userId 用户ID
     * @return 模板
     */
    CompanyTemplatePO selectByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    /**
     * 插入模板
     *
     * @param template 模板
     * @return 插入行数
     */
    int insert(CompanyTemplatePO template);

    /**
     * 更新模板
     *
     * @param template 模板
     * @return 更新行数
     */
    int update(CompanyTemplatePO template);

    /**
     * 根据ID和用户ID更新模板（用于权限验证）
     *
     * @param template 模板
     * @return 更新行数
     */
    int updateByIdAndUserId(CompanyTemplatePO template);

    /**
     * 删除模板
     *
     * @param id 模板ID
     * @return 删除行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 根据ID和用户ID删除模板（用于权限验证）
     *
     * @param id 模板ID
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByIdAndUserId(@Param("id") String id, @Param("userId") String userId);
}

