package com.pbad.asset.validator;

/**
 * 抽象校验器基类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public abstract class AbstractValidator implements Validator {
    
    /**
     * 下一个校验器
     */
    protected Validator next;
    
    @Override
    public void setNext(Validator next) {
        this.next = next;
    }
    
    @Override
    public ValidationResult validate(Object dto) {
        ValidationResult result = doValidate(dto);
        
        // 如果当前校验失败，直接返回
        if (!result.isValid()) {
            return result;
        }
        
        // 如果还有下一个校验器，继续校验
        if (next != null) {
            ValidationResult nextResult = next.validate(dto);
            // 合并校验结果
            result.merge(nextResult);
        }
        
        return result;
    }
    
    /**
     * 执行具体校验逻辑（由子类实现）
     *
     * @param dto 资产DTO
     * @return 校验结果
     */
    protected abstract ValidationResult doValidate(Object dto);
}

