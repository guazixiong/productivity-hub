package com.pbad.acl.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * ACL校验器链构建器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component("aclValidatorChainBuilder")
@RequiredArgsConstructor
public class ValidatorChainBuilder {

    private final ObjectProvider<MenuLevelValidator> menuLevelValidatorProvider;
    private final ObjectProvider<MenuCircularReferenceValidator> menuCircularReferenceValidatorProvider;
    private final ObjectProvider<MenuUniqueValidator> menuUniqueValidatorProvider;
    private final ObjectProvider<MenuIdempotentValidator> menuIdempotentValidatorProvider;
    private final ObjectProvider<RoleUniqueValidator> roleUniqueValidatorProvider;
    private final ObjectProvider<RoleBuiltInTypeValidator> roleBuiltInTypeValidatorProvider;
    private final ObjectProvider<RoleIdempotentValidator> roleIdempotentValidatorProvider;

    /**
     * 构建校验器链（用于创建菜单）
     *
     * @return 校验器链
     */
    public Validator buildCreateChain() {
        MenuLevelValidator levelValidator = menuLevelValidatorProvider.getObject();
        MenuUniqueValidator uniqueValidator = menuUniqueValidatorProvider.getObject();
        MenuIdempotentValidator idempotentValidator = menuIdempotentValidatorProvider.getObject();

        // 构建链：层级校验 -> 唯一性校验 -> 幂等键校验
        levelValidator.setNext(uniqueValidator);
        uniqueValidator.setNext(idempotentValidator);

        return levelValidator;
    }

    /**
     * 构建校验器链（用于更新菜单）
     *
     * @return 校验器链
     */
    public Validator buildUpdateChain() {
        MenuLevelValidator levelValidator = menuLevelValidatorProvider.getObject();
        MenuCircularReferenceValidator circularRefValidator = menuCircularReferenceValidatorProvider.getObject();
        MenuUniqueValidator uniqueValidator = menuUniqueValidatorProvider.getObject();

        // 构建链：层级校验 -> 循环引用校验 -> 唯一性校验
        levelValidator.setNext(circularRefValidator);
        circularRefValidator.setNext(uniqueValidator);

        return levelValidator;
    }

    /**
     * 构建校验器链（用于创建角色）
     *
     * @return 校验器链
     */
    public Validator buildRoleCreateChain() {
        RoleUniqueValidator uniqueValidator = roleUniqueValidatorProvider.getObject();
        RoleIdempotentValidator idempotentValidator = roleIdempotentValidatorProvider.getObject();

        // 构建链：唯一性校验 -> 幂等键校验
        uniqueValidator.setNext(idempotentValidator);

        return uniqueValidator;
    }

    /**
     * 构建校验器链（用于更新角色）
     *
     * @return 校验器链
     */
    public Validator buildRoleUpdateChain() {
        RoleUniqueValidator uniqueValidator = roleUniqueValidatorProvider.getObject();
        RoleBuiltInTypeValidator builtInTypeValidator = roleBuiltInTypeValidatorProvider.getObject();

        // 构建链：唯一性校验 -> 内置类型约束校验
        uniqueValidator.setNext(builtInTypeValidator);

        return uniqueValidator;
    }
}

