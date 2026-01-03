package com.pbad.basic.asset.controller;

import com.pbad.asset.domain.dto.*;
import com.pbad.asset.domain.vo.*;
import com.pbad.asset.service.*;
import com.pbad.auth.util.UserRoleUtil;
import common.core.domain.ApiResponse;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产管理控制器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@RestController
@RequestMapping("/api/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetCategoryService categoryService;
    private final AssetService assetService;
    private final AssetAdditionalFeeService additionalFeeService;
    private final AssetSoldService soldService;
    private final AssetStatisticsService statisticsService;
    private final UserRoleUtil userRoleUtil;

    /**
     * 检查当前用户是否为管理员
     */
    private void checkAdmin() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "用户未登录");
        }
        if (!userRoleUtil.isAdmin(userId)) {
            throw new BusinessException("403", "仅管理员可以访问资产分类管理");
        }
    }

    // ==================== 资产分类管理 ====================

    /**
     * 获取所有分类（仅管理员）
     */
    @GetMapping("/categories")
    public ApiResponse<List<AssetCategoryVO>> getAllCategories() {
        checkAdmin();
        List<AssetCategoryVO> categories = categoryService.getAllCategories();
        return ApiResponse.ok(categories);
    }

    /**
     * 获取所有分类（用于选择，所有用户可访问）
     */
    @GetMapping("/categories/selectable")
    public ApiResponse<List<AssetCategoryVO>> getSelectableCategories() {
        List<AssetCategoryVO> categories = categoryService.getAllCategories();
        return ApiResponse.ok(categories);
    }

    /**
     * 根据ID获取分类（仅管理员）
     */
    @GetMapping("/categories/{id}")
    public ApiResponse<AssetCategoryVO> getCategoryById(@PathVariable String id) {
        checkAdmin();
        AssetCategoryVO category = categoryService.getCategoryById(id);
        return ApiResponse.ok(category);
    }

    /**
     * 创建分类（仅管理员）
     */
    @PostMapping("/categories")
    public ApiResponse<AssetCategoryVO> createCategory(@RequestBody AssetCategoryCreateDTO createDTO) {
        checkAdmin();
        AssetCategoryVO category = categoryService.createCategory(createDTO);
        return ApiResponse.ok(category);
    }

    /**
     * 更新分类（仅管理员）
     */
    @PutMapping("/categories")
    public ApiResponse<AssetCategoryVO> updateCategory(@RequestBody AssetCategoryUpdateDTO updateDTO) {
        checkAdmin();
        AssetCategoryVO category = categoryService.updateCategory(updateDTO);
        return ApiResponse.ok(category);
    }

    /**
     * 删除分类（仅管理员）
     */
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        checkAdmin();
        categoryService.deleteCategory(id);
        return ApiResponse.ok(null);
    }

    // ==================== 资产管理 ====================

    /**
     * 分页查询资产列表
     */
    @GetMapping("/assets")
    public ApiResponse<AssetPageVO> getAssetPage(@ModelAttribute AssetQueryDTO queryDTO) {
        AssetPageVO pageVO = assetService.getAssetPage(queryDTO);
        return ApiResponse.ok(pageVO);
    }

    /**
     * 根据ID获取资产详情
     */
    @GetMapping("/assets/{id}")
    public ApiResponse<AssetDetailVO> getAssetById(@PathVariable String id) {
        AssetDetailVO asset = assetService.getAssetById(id);
        return ApiResponse.ok(asset);
    }

    /**
     * 创建资产
     */
    @PostMapping("/assets")
    public ApiResponse<AssetDetailVO> createAsset(@RequestBody AssetCreateDTO createDTO) {
        AssetDetailVO asset = assetService.createAsset(createDTO);
        return ApiResponse.ok(asset);
    }

    /**
     * 更新资产
     */
    @PutMapping("/assets")
    public ApiResponse<AssetDetailVO> updateAsset(@RequestBody AssetUpdateDTO updateDTO) {
        AssetDetailVO asset = assetService.updateAsset(updateDTO);
        return ApiResponse.ok(asset);
    }

    /**
     * 删除资产
     */
    @DeleteMapping("/assets/{id}")
    public ApiResponse<Void> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return ApiResponse.ok(null);
    }

    /**
     * 批量删除资产
     */
    @DeleteMapping("/assets/batch")
    public ApiResponse<Void> batchDeleteAssets(@RequestBody List<String> ids) {
        assetService.batchDeleteAssets(ids);
        return ApiResponse.ok(null);
    }

    /**
     * 更新资产状态
     */
    @PutMapping("/assets/status")
    public ApiResponse<Void> updateAssetStatus(@RequestBody AssetStatusUpdateDTO updateStatusDTO) {
        assetService.updateAssetStatus(updateStatusDTO);
        return ApiResponse.ok(null);
    }

    // ==================== 附加费用管理 ====================

    /**
     * 根据资产ID获取附加费用列表
     */
    @GetMapping("/assets/{assetId}/fees")
    public ApiResponse<List<AssetAdditionalFeeVO>> getFeesByAssetId(@PathVariable String assetId) {
        List<AssetAdditionalFeeVO> fees = additionalFeeService.getFeesByAssetId(assetId);
        return ApiResponse.ok(fees);
    }

    /**
     * 创建附加费用
     */
    @PostMapping("/fees")
    public ApiResponse<AssetAdditionalFeeVO> createFee(@RequestBody AssetAdditionalFeeCreateDTO createDTO) {
        AssetAdditionalFeeVO fee = additionalFeeService.createFee(createDTO);
        return ApiResponse.ok(fee);
    }

    /**
     * 更新附加费用
     */
    @PutMapping("/fees")
    public ApiResponse<AssetAdditionalFeeVO> updateFee(@RequestBody AssetAdditionalFeeUpdateDTO updateDTO) {
        AssetAdditionalFeeVO fee = additionalFeeService.updateFee(updateDTO);
        return ApiResponse.ok(fee);
    }

    /**
     * 删除附加费用
     */
    @DeleteMapping("/fees/{id}")
    public ApiResponse<Void> deleteFee(@PathVariable String id) {
        additionalFeeService.deleteFee(id);
        return ApiResponse.ok(null);
    }

    // ==================== 资产卖出管理 ====================

    /**
     * 根据资产ID获取卖出记录
     */
    @GetMapping("/assets/{assetId}/sold")
    public ApiResponse<AssetSoldVO> getSoldByAssetId(@PathVariable String assetId) {
        AssetSoldVO sold = soldService.getSoldByAssetId(assetId);
        return ApiResponse.ok(sold);
    }

    /**
     * 创建卖出记录
     */
    @PostMapping("/sold")
    public ApiResponse<AssetSoldVO> createSold(@RequestBody AssetSoldCreateDTO createDTO) {
        AssetSoldVO sold = soldService.createSold(createDTO);
        return ApiResponse.ok(sold);
    }

    /**
     * 更新卖出记录
     */
    @PutMapping("/sold")
    public ApiResponse<AssetSoldVO> updateSold(@RequestBody AssetSoldUpdateDTO updateDTO) {
        AssetSoldVO sold = soldService.updateSold(updateDTO);
        return ApiResponse.ok(sold);
    }

    // ==================== 资产统计 ====================

    /**
     * 获取资产统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<AssetStatisticsVO> getStatistics() {
        AssetStatisticsVO statistics = statisticsService.getStatistics();
        return ApiResponse.ok(statistics);
    }
}

