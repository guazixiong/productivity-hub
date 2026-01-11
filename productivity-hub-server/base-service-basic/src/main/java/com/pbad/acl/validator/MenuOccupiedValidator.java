package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.mapper.AclMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 菜单占用校验器（检查菜单是否被角色占用，用于删除操作）.
 * 注意：此校验器主要用于删除前的校验，在Service层调用
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class MenuOccupiedValidator {

    private final AclMenuMapper menuMapper;

    /**
     * 校验菜单是否被占用
     *
     * @param menuId 菜单ID
     * @return 校验结果
     */
    public ValidationResult validate(Long menuId) {
        ValidationResult result = new ValidationResult();

        if (menuId == null) {
            result.addError("菜单ID不能为空");
            return result;
        }

        // 检查菜单是否被角色占用
        int count = menuMapper.countRoleMenuByMenuId(menuId);
        if (count > 0) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_5093) + 
                    String.format("（已被%d个角色占用）", count));
        }

        return result;
    }
}

