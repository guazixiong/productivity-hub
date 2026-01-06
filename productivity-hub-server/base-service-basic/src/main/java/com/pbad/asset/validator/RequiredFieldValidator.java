package com.pbad.asset.validator;

import com.pbad.asset.domain.dto.AssetCreateDTO;
import com.pbad.asset.domain.dto.AssetUpdateDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 必填字段校验器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@Scope("prototype")
public class RequiredFieldValidator extends AbstractValidator {
    
    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();
        
        if (dto == null) {
            result.addError("请求参数不能为空");
            return result;
        }
        
        // 处理AssetCreateDTO
        if (dto instanceof AssetCreateDTO) {
            AssetCreateDTO createDTO = (AssetCreateDTO) dto;
            
            if (!StringUtils.hasText(createDTO.getName())) {
                result.addError("资产名称不能为空");
            }
            
            if (!StringUtils.hasText(createDTO.getCategoryId())) {
                result.addError("分类ID不能为空");
            }
            
            if (createDTO.getPrice() == null) {
                result.addError("资产价格不能为空");
            }
            
            if (!StringUtils.hasText(createDTO.getPurchaseDate())) {
                result.addError("购买日期不能为空");
            }
        }
        
        // 处理AssetUpdateDTO
        if (dto instanceof AssetUpdateDTO) {
            AssetUpdateDTO updateDTO = (AssetUpdateDTO) dto;
            
            if (!StringUtils.hasText(updateDTO.getId())) {
                result.addError("资产ID不能为空");
            }
            
            if (updateDTO.getVersion() == null) {
                result.addError("版本号不能为空");
            }
            
            if (!StringUtils.hasText(updateDTO.getName())) {
                result.addError("资产名称不能为空");
            }
            
            if (!StringUtils.hasText(updateDTO.getCategoryId())) {
                result.addError("分类ID不能为空");
            }
            
            if (updateDTO.getPrice() == null) {
                result.addError("资产价格不能为空");
            }
            
            if (!StringUtils.hasText(updateDTO.getPurchaseDate())) {
                result.addError("购买日期不能为空");
            }
        }
        
        return result;
    }
}

