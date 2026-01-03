package com.pbad.asset.mapper;

import com.pbad.asset.domain.po.AssetCategoryPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 资产分类 Mapper 接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetCategoryMapper {

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    List<AssetCategoryPO> selectAll();

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类
     */
    AssetCategoryPO selectById(@Param("id") String id);

    /**
     * 根据分类名称查询分类（用于检查重复）
     *
     * @param name 分类名称
     * @return 分类
     */
    AssetCategoryPO selectByName(@Param("name") String name);

    /**
     * 根据分类名称和父分类ID查询分类（用于检查同一父分类下的重复）
     *
     * @param name     分类名称
     * @param parentId 父分类ID（可为空，表示大分类）
     * @return 分类
     */
    AssetCategoryPO selectByNameAndParentId(@Param("name") String name, @Param("parentId") String parentId);

    /**
     * 插入分类
     *
     * @param category 分类
     * @return 插入行数
     */
    int insertCategory(AssetCategoryPO category);

    /**
     * 更新分类
     *
     * @param category 分类
     * @return 更新行数
     */
    int updateCategory(AssetCategoryPO category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除行数
     */
    int deleteCategory(@Param("id") String id);

    /**
     * 根据父分类ID查询子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<AssetCategoryPO> selectByParentId(@Param("parentId") String parentId);

    /**
     * 统计分类下的资产数量
     *
     * @param categoryId 分类ID
     * @return 资产数量
     */
    int countAssetsByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 统计分类及其所有子分类下的总资产数量
     *
     * @param categoryId 分类ID
     * @return 总资产数量（包括当前分类和所有子分类）
     */
    int countTotalAssetsByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 批量统计所有分类下的资产数量
     *
     * @return Map，key为分类ID，value为资产数量
     */
    List<Map<String, Object>> countAssetsByCategoryIds();
}

