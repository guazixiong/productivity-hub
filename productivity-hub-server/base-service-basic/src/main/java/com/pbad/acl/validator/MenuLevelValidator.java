package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.mapper.AclMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 菜单层级校验器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class MenuLevelValidator extends AbstractValidator {

    private final AclMenuMapper menuMapper;

    /**
     * 最大菜单层级（默认3，可通过配置修改）
     */
    @Value("${app.acl.menu.max-level:3}")
    private int maxLevel;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        Long parentId = null;
        Long menuId = null;

        // 处理创建DTO
        if (dto instanceof AclMenuCreateDTO) {
            AclMenuCreateDTO createDTO = (AclMenuCreateDTO) dto;
            parentId = createDTO.getParentId();
        }

        // 处理更新DTO
        if (dto instanceof AclMenuUpdateDTO) {
            AclMenuUpdateDTO updateDTO = (AclMenuUpdateDTO) dto;
            menuId = Long.parseLong(updateDTO.getId());
            parentId = updateDTO.getParentId();
        }

        // 如果没有父菜单，层级为1，直接通过
        if (parentId == null) {
            return result;
        }

        // 计算新菜单的层级深度
        int parentLevel = menuMapper.calculateMenuLevel(parentId);
        int newLevel = parentLevel + 1;

        // 检查是否超过最大层级
        if (newLevel > maxLevel) {
            result.addError(AclErrorCode.getErrorMessage(AclErrorCode.ACL_4003) + 
                    String.format("（当前层级：%d，最大层级：%d）", newLevel, maxLevel));
        }

        return result;
    }
}

