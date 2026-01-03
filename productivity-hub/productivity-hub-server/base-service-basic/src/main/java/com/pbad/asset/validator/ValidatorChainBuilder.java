package com.pbad.asset.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 校验器链构建器.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class ValidatorChainBuilder {

    private final ObjectProvider<RequiredFieldValidator> requiredFieldValidatorProvider;
    private final ObjectProvider<FieldFormatValidator> fieldFormatValidatorProvider;
    private final ObjectProvider<BusinessRuleValidator> businessRuleValidatorProvider;

    /**
     * 构建校验器链（用于创建资产）
     *
     * @return 校验器链
     */
    public Validator buildCreateChain() {
        return buildChain();
    }

    /**
     * 构建校验器链（用于更新资产）
     *
     * @return 校验器链
     */
    public Validator buildUpdateChain() {
        return buildChain();
    }

    /**
     * 构建校验器链（通用，根据DTO类型自动选择）
     *
     * @return 校验器链
     */
    public Validator build() {
        return buildChain();
    }

    /**
     * 每次调用创建全新的责任链实例，避免并发和状态污染。
     */
    private Validator buildChain() {
        RequiredFieldValidator requiredFieldValidator = requiredFieldValidatorProvider.getObject();
        FieldFormatValidator fieldFormatValidator = fieldFormatValidatorProvider.getObject();
        BusinessRuleValidator businessRuleValidator = businessRuleValidatorProvider.getObject();

        requiredFieldValidator.setNext(fieldFormatValidator);
        fieldFormatValidator.setNext(businessRuleValidator);

        return requiredFieldValidator;
    }
}

