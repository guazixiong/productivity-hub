package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.mapper.AclRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色占用校验器（检查角色是否被用户占用，用于删除操作）.
 * 注意：此校验器主要用于删除前的校验，在Service层调用
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class RoleOccupiedValidator {

    private final AclRoleMapper roleMapper;

    /**
     * 校验角色是否被占用
     *
     * @param roleId 角色ID
     * @return 校验结果
     */
    public ValidationResult validate(Long roleId) {
        ValidationResult result = new ValidationResult();

        if (roleId == null) {
            result.addError("角色ID不能为空");
            return result;
        }

        // 检查角色是否被用户占用
        int count = roleMapper.countUserRoleByRoleId(roleId);
        if (count > 0) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5093) + 
                    String.format("（已被%d个用户占用）", count));
        }

        return result;
    }
}

