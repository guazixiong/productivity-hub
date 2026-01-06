package com.pbad.asset.validator;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验结果.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public class ValidationResult {
    /**
     * 是否校验通过
     */
    private boolean valid;
    
    /**
     * 错误信息列表
     */
    private List<String> errors;
    
    /**
     * 构造函数，默认校验通过
     */
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }
    
    /**
     * 添加错误信息
     *
     * @param error 错误信息
     */
    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }
    
    /**
     * 是否校验通过
     *
     * @return true表示校验通过，false表示校验失败
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 获取错误信息列表
     *
     * @return 错误信息列表
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * 获取错误信息字符串（多个错误用分号分隔）
     *
     * @return 错误信息字符串
     */
    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return "";
        }
        return String.join("; ", errors);
    }
    
    /**
     * 创建成功的校验结果
     *
     * @return 校验结果
     */
    public static ValidationResult success() {
        return new ValidationResult();
    }
    
    /**
     * 创建失败的校验结果
     *
     * @param error 错误信息
     * @return 校验结果
     */
    public static ValidationResult fail(String error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }
    
    /**
     * 合并另一个校验结果
     *
     * @param other 另一个校验结果
     */
    public void merge(ValidationResult other) {
        if (other != null && !other.isValid()) {
            this.valid = false;
            this.errors.addAll(other.getErrors());
        }
    }
    
    /**
     * 设置校验状态（用于合并结果）
     *
     * @param valid 是否校验通过
     */
    void setValid(boolean valid) {
        this.valid = valid;
    }
}

