package com.pbad.asset.validator;

import com.pbad.asset.domain.dto.AssetCreateDTO;
import com.pbad.asset.domain.dto.AssetUpdateDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 字段格式校验器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@Scope("prototype")
public class FieldFormatValidator extends AbstractValidator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();
        
        if (dto == null) {
            return result;
        }
        
        // 处理AssetCreateDTO
        if (dto instanceof AssetCreateDTO) {
            AssetCreateDTO createDTO = (AssetCreateDTO) dto;
            validatePrice(createDTO.getPrice(), result);
            validateDate(createDTO.getPurchaseDate(), "购买日期", result);
            validateDate(createDTO.getWarrantyEndDate(), "保修截止日期", result);
            validateDate(createDTO.getUsageDate(), "使用日期", result);
            validateDate(createDTO.getRetiredDate(), "退役日期", result);
            validateUsageCount(createDTO.getExpectedUsageCount(), result);
            validateNameLength(createDTO.getName(), result);
            validateImageUrl(createDTO.getImage(), result);
            validateRemarkLength(createDTO.getRemark(), result);
            validateDateLogic(createDTO.getPurchaseDate(), createDTO.getWarrantyEndDate(), 
                    createDTO.getUsageDate(), createDTO.getRetiredDate(), result);
        }
        
        // 处理AssetUpdateDTO
        if (dto instanceof AssetUpdateDTO) {
            AssetUpdateDTO updateDTO = (AssetUpdateDTO) dto;
            validatePrice(updateDTO.getPrice(), result);
            validateDate(updateDTO.getPurchaseDate(), "购买日期", result);
            validateDate(updateDTO.getWarrantyEndDate(), "保修截止日期", result);
            validateDate(updateDTO.getUsageDate(), "使用日期", result);
            validateDate(updateDTO.getRetiredDate(), "退役日期", result);
            validateUsageCount(updateDTO.getExpectedUsageCount(), result);
            validateNameLength(updateDTO.getName(), result);
            validateImageUrl(updateDTO.getImage(), result);
            validateRemarkLength(updateDTO.getRemark(), result);
            validateDateLogic(updateDTO.getPurchaseDate(), updateDTO.getWarrantyEndDate(), 
                    updateDTO.getUsageDate(), updateDTO.getRetiredDate(), result);
        }
        
        return result;
    }
    
    /**
     * 校验价格格式
     */
    private void validatePrice(BigDecimal price, ValidationResult result) {
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                result.addError("资产价格必须大于0");
            }
            // 检查小数位数（最多2位小数）
            if (price.scale() > 2) {
                result.addError("资产价格最多保留2位小数");
            }
        }
    }
    
    /**
     * 校验日期格式
     */
    private void validateDate(String dateStr, String fieldName, ValidationResult result) {
        if (StringUtils.hasText(dateStr)) {
            try {
                Date date = DATE_FORMAT.parse(dateStr);
                // 检查购买日期不能是未来日期
                if ("购买日期".equals(fieldName) && date.after(new Date())) {
                    result.addError("购买日期不能是未来日期");
                }
            } catch (ParseException e) {
                result.addError(fieldName + "格式错误，应为yyyy-MM-dd");
            }
        }
    }
    
    /**
     * 校验使用次数
     */
    private void validateUsageCount(Integer expectedUsageCount, ValidationResult result) {
        if (expectedUsageCount != null && expectedUsageCount <= 0) {
            result.addError("预计使用次数必须大于0");
        }
    }
    
    /**
     * 校验名称长度
     */
    private void validateNameLength(String name, ValidationResult result) {
        if (StringUtils.hasText(name)) {
            if (name.length() > 100) {
                result.addError("资产名称长度不能超过100个字符");
            }
        }
    }
    
    /**
     * 校验图片URL长度
     */
    private void validateImageUrl(String image, ValidationResult result) {
        if (StringUtils.hasText(image) && image.length() > 500) {
            result.addError("资产图片URL长度不能超过500个字符");
        }
    }
    
    /**
     * 校验备注长度
     */
    private void validateRemarkLength(String remark, ValidationResult result) {
        if (StringUtils.hasText(remark) && remark.length() > 500) {
            result.addError("备注长度不能超过500个字符");
        }
    }
    
    /**
     * 校验日期逻辑关系
     */
    private void validateDateLogic(String purchaseDateStr, String warrantyEndDateStr, 
                                   String usageDateStr, String retiredDateStr, ValidationResult result) {
        try {
            Date purchaseDate = null;
            Date warrantyEndDate = null;
            Date usageDate = null;
            Date retiredDate = null;
            
            if (StringUtils.hasText(purchaseDateStr)) {
                purchaseDate = DATE_FORMAT.parse(purchaseDateStr);
            }
            if (StringUtils.hasText(warrantyEndDateStr)) {
                warrantyEndDate = DATE_FORMAT.parse(warrantyEndDateStr);
            }
            if (StringUtils.hasText(usageDateStr)) {
                usageDate = DATE_FORMAT.parse(usageDateStr);
            }
            if (StringUtils.hasText(retiredDateStr)) {
                retiredDate = DATE_FORMAT.parse(retiredDateStr);
            }
            
            // 保修截止日期不能早于购买日期
            if (purchaseDate != null && warrantyEndDate != null) {
                if (warrantyEndDate.before(purchaseDate)) {
                    result.addError("保修截止日期不能早于购买日期");
                }
            }
            
            // 使用日期不能早于购买日期
            if (purchaseDate != null && usageDate != null) {
                if (usageDate.before(purchaseDate)) {
                    result.addError("使用日期不能早于购买日期");
                }
            }
            
            // 退役日期不能早于购买日期
            if (purchaseDate != null && retiredDate != null) {
                if (retiredDate.before(purchaseDate)) {
                    result.addError("退役日期不能早于购买日期");
                }
            }
            
            // 退役日期不能早于使用日期
            if (usageDate != null && retiredDate != null) {
                if (retiredDate.before(usageDate)) {
                    result.addError("退役日期不能早于使用日期");
                }
            }
        } catch (ParseException e) {
            // 日期格式错误已在validateDate中处理，这里忽略
        }
    }
}

