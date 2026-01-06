package com.pbad.asset.validator;

import com.pbad.asset.domain.dto.AssetCreateDTO;
import com.pbad.asset.domain.dto.AssetUpdateDTO;
import com.pbad.asset.domain.po.AssetCategoryPO;
import com.pbad.asset.mapper.AssetCategoryMapper;
import com.pbad.asset.mapper.AssetMapper;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 业务规则校验器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BusinessRuleValidator extends AbstractValidator {
    
    private final AssetCategoryMapper categoryMapper;
    private final AssetMapper assetMapper;
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        return userId;
    }
    
    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();
        
        if (dto == null) {
            return result;
        }
        
        String userId = getCurrentUserId();
        if (userId == null) {
            result.addError("用户未登录");
            return result;
        }
        
        // 处理AssetCreateDTO
        if (dto instanceof AssetCreateDTO) {
            AssetCreateDTO createDTO = (AssetCreateDTO) dto;
            validateCategoryExists(createDTO.getCategoryId(), userId, result);
            validateWarrantyLogic(createDTO.getWarrantyEnabled(), createDTO.getWarrantyEndDate(), result);
            validateDepreciationLogic(createDTO.getDepreciationByUsageCount(), 
                    createDTO.getExpectedUsageCount(), createDTO.getDepreciationByUsageDate(), 
                    createDTO.getUsageDate(), result);
        }
        
        // 处理AssetUpdateDTO
        if (dto instanceof AssetUpdateDTO) {
            AssetUpdateDTO updateDTO = (AssetUpdateDTO) dto;
            validateCategoryExists(updateDTO.getCategoryId(), userId, result);
            validateWarrantyLogic(updateDTO.getWarrantyEnabled(), updateDTO.getWarrantyEndDate(), result);
            validateDepreciationLogic(updateDTO.getDepreciationByUsageCount(), 
                    updateDTO.getExpectedUsageCount(), updateDTO.getDepreciationByUsageDate(), 
                    updateDTO.getUsageDate(), result);
            validateAssetExists(updateDTO.getId(), userId, result);
        }
        
        return result;
    }
    
    /**
     * 校验分类是否存在
     */
    private void validateCategoryExists(String categoryId, String userId, ValidationResult result) {
        if (StringUtils.hasText(categoryId)) {
            AssetCategoryPO category = categoryMapper.selectById(categoryId);
            if (category == null) {
                result.addError("分类不存在");
            }
        }
    }
    
    /**
     * 校验保修逻辑
     */
    private void validateWarrantyLogic(Boolean warrantyEnabled, String warrantyEndDate, ValidationResult result) {
        if (warrantyEnabled != null && warrantyEnabled) {
            if (!StringUtils.hasText(warrantyEndDate)) {
                result.addError("启用保修时，保修截止日期不能为空");
            }
        }
    }
    
    /**
     * 校验贬值计算逻辑
     */
    private void validateDepreciationLogic(Boolean depreciationByUsageCount, Integer expectedUsageCount,
                                          Boolean depreciationByUsageDate, String usageDate, ValidationResult result) {
        // 如果按使用次数计算贬值，预计使用次数不能为空
        if (depreciationByUsageCount != null && depreciationByUsageCount) {
            if (expectedUsageCount == null || expectedUsageCount <= 0) {
                result.addError("按使用次数计算贬值时，预计使用次数必须大于0");
            }
        }
        
        // 如果按使用日期计算贬值，使用日期不能为空
        if (depreciationByUsageDate != null && depreciationByUsageDate) {
            if (!StringUtils.hasText(usageDate)) {
                result.addError("按使用日期计算贬值时，使用日期不能为空");
            }
        }
    }
    
    /**
     * 校验资产是否存在（用于更新场景）
     */
    private void validateAssetExists(String assetId, String userId, ValidationResult result) {
        if (StringUtils.hasText(assetId)) {
            // 注意：这里只做基本的存在性检查，不检查删除状态，因为删除状态检查应该在Service层进行
            // 这里主要是为了在更新前确保资产ID有效
        }
    }
}

