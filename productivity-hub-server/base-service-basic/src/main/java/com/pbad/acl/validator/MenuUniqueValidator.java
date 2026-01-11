package com.pbad.acl.validator;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.domain.po.AclMenuPO;
import com.pbad.acl.mapper.AclMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单唯一性校验器（检查同一父级下菜单名称是否唯一）.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Component
@RequiredArgsConstructor
public class MenuUniqueValidator extends AbstractValidator {

    private final AclMenuMapper menuMapper;

    @Override
    protected ValidationResult doValidate(Object dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            return result;
        }

        Long parentId = null;
        String name = null;
        Long menuId = null;

        // 处理创建DTO
        if (dto instanceof AclMenuCreateDTO) {
            AclMenuCreateDTO createDTO = (AclMenuCreateDTO) dto;
            parentId = createDTO.getParentId();
            name = createDTO.getName();
        }

        // 处理更新DTO
        if (dto instanceof AclMenuUpdateDTO) {
            AclMenuUpdateDTO updateDTO = (AclMenuUpdateDTO) dto;
            menuId = Long.parseLong(updateDTO.getId());
            parentId = updateDTO.getParentId();
            name = updateDTO.getName();
        }

        if (name == null || name.trim().isEmpty()) {
            return result;
        }

        // 查询同一父级下的所有菜单
        List<AclMenuPO> siblings = menuMapper.selectByParentId(parentId);
        
        // 检查是否有同名菜单（排除自己）
        for (AclMenuPO sibling : siblings) {
            if (sibling.getName().equals(name.trim())) {
                // 如果是更新操作，排除自己
                if (menuId != null && sibling.getId().equals(menuId)) {
                    continue;
                }
                result.addError(String.format("同一父级下已存在名为'%s'的菜单", name));
                break;
            }
        }

        return result;
    }
}

