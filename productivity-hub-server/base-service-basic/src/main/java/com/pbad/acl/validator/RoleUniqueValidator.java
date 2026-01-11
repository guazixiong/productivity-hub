package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclRoleCreateDTO;
import com.pbad.acl.domain.dto.AclRoleUpdateDTO;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色唯一性校验器（检查角色名称是否唯一）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class RoleUniqueValidator extends AbstractValidator {

    private final AclRoleMapper roleMapper;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        String name = null;
        Long roleId = null;

        // 处理创建DTO
        if (dto instanceof AclRoleCreateDTO) {
            AclRoleCreateDTO createDTO = (AclRoleCreateDTO) dto;
            name = createDTO.getName();
        }

        // 处理更新DTO
        if (dto instanceof AclRoleUpdateDTO) {
            AclRoleUpdateDTO updateDTO = (AclRoleUpdateDTO) dto;
            roleId = Long.parseLong(updateDTO.getId());
            name = updateDTO.getName();
        }

        if (name == null || name.trim().isEmpty()) {
            return result;
        }

        // 查询是否存在同名角色
        AclRolePO existingRole = roleMapper.selectByName(name.trim());
        
        if (existingRole != null) {
            // 如果是更新操作，排除自己
            if (roleId != null && existingRole.getId().equals(roleId)) {
                return result;
            }
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5091));
        }

        return result;
    }
}

