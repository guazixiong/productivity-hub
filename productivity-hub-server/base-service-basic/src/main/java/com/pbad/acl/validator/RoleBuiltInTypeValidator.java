package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclRoleUpdateDTO;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色内置类型约束校验器（内置角色类型不可修改，内置角色不可删除）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class RoleBuiltInTypeValidator extends AbstractValidator {

    private final AclRoleMapper roleMapper;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        // 只对更新DTO进行内置类型校验
        if (!(dto instanceof AclRoleUpdateDTO)) {
            return result;
        }

        AclRoleUpdateDTO updateDTO = (AclRoleUpdateDTO) dto;
        Long roleId = Long.parseLong(updateDTO.getId());

        // 查询角色
        AclRolePO role = roleMapper.selectById(roleId);
        if (role == null) {
            return result;
        }

        // 检查是否为内置角色
        String originalType = role.getType();
        if (RoleType.ADMIN.getCode().equals(originalType) || 
            RoleType.USER.getCode().equals(originalType)) {
            
            // 内置角色的类型不可修改
            String newType = updateDTO.getType();
            if (!originalType.equals(newType)) {
                result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5098));
            }
        }

        return result;
    }

    /**
     * 检查角色是否为内置角色（用于删除校验）
     *
     * @param roleId 角色ID
     * @return 校验结果
     */
    public ValidationResult validateForDelete(Long roleId) {
        ValidationResult result = new ValidationResult();

        if (roleId == null) {
            return result;
        }

        // 查询角色
        AclRolePO role = roleMapper.selectById(roleId);
        if (role == null) {
            return result;
        }

        // 检查是否为内置角色
        String type = role.getType();
        if (RoleType.ADMIN.getCode().equals(type) || 
            RoleType.USER.getCode().equals(type)) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5097));
        }

        return result;
    }
}

