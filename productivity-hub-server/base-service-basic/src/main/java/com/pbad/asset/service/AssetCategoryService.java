package com.pbad.asset.service;

import com.pbad.asset.domain.dto.AssetCategoryCreateDTO;
import com.pbad.asset.domain.dto.AssetCategoryUpdateDTO;
import com.pbad.asset.domain.vo.AssetCategoryVO;

import java.util.List;

/**
 * 资产分类服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface AssetCategoryService {

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<AssetCategoryVO> getAllCategories();

    /**
     * 根据ID获取分类
     *
     * @param id 分类ID
     * @return 分类
     */
    AssetCategoryVO getCategoryById(String id);

    /**
     * 创建分类
     *
     * @param createDTO 创建DTO
     * @return 创建的分类
     */
    AssetCategoryVO createCategory(AssetCategoryCreateDTO createDTO);

    /**
     * 更新分类
     *
     * @param updateDTO 更新DTO
     * @return 更新后的分类
     */
    AssetCategoryVO updateCategory(AssetCategoryUpdateDTO updateDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(String id);
}

