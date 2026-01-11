package com.pbad.acl.validator;

/**
 * ACL校验器接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface Validator {
    /**
     * 校验数据
     *
     * @param dto 菜单DTO（可以是AclMenuCreateDTO或AclMenuUpdateDTO）
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

