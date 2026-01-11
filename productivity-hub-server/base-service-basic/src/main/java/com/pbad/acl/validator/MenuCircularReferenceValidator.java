package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.mapper.AclMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 菜单循环引用校验器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class MenuCircularReferenceValidator extends AbstractValidator {

    private final AclMenuMapper menuMapper;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        Long menuId = null;
        Long parentId = null;

        // 处理创建DTO（创建时不需要检查循环引用，因为没有ID）
        if (dto instanceof AclMenuCreateDTO) {
            // 创建时不需要检查循环引用
            return result;
        }

        // 处理更新DTO
        if (dto instanceof AclMenuUpdateDTO) {
            AclMenuUpdateDTO updateDTO = (AclMenuUpdateDTO) dto;
            menuId = Long.parseLong(updateDTO.getId());
            parentId = updateDTO.getParentId();
        }

        // 如果没有父菜单，不需要检查循环引用
        if (parentId == null || menuId == null) {
            return result;
        }

        // 检查是否存在循环引用
        int circularRef = menuMapper.checkCircularReference(menuId, parentId);
        if (circularRef > 0) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5092));
        }

        return result;
    }
}

