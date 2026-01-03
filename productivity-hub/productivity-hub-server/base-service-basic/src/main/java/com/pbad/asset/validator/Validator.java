package com.pbad.asset.validator;

/**
 * 校验器接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface Validator {
    /**
     * 校验数据
     *
     * @param dto 资产DTO（可以是AssetCreateDTO或AssetUpdateDTO）
     * @return 校验结果
     */
    ValidationResult validate(Object dto);
    
    /**
     * 设置下一个校验器
     *
     * @param next 下一个校验器
     */
    void setNext(Validator next);
}

