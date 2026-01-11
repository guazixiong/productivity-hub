package com.pbad.acl.service.impl;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclRoleCreateDTO;
import com.pbad.acl.domain.dto.AclRoleMenuBindDTO;
import com.pbad.acl.domain.dto.AclRoleUpdateDTO;
import com.pbad.acl.domain.enums.AclStatus;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.domain.vo.AclRoleVO;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclRoleMenuMapper;
import com.pbad.acl.service.AclRoleService;
import com.pbad.acl.validator.RoleBuiltInTypeValidator;
import com.pbad.acl.validator.RoleIdempotentValidator;
import com.pbad.acl.validator.RoleOccupiedValidator;
import com.pbad.acl.validator.ValidationResult;
import com.pbad.acl.validator.Validator;
import com.pbad.acl.validator.ValidatorChainBuilder;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ACL角色服务实现类.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AclRoleServiceImpl implements AclRoleService {

    private final AclRoleMapper roleMapper;
    private final AclRoleMenuMapper roleMenuMapper;
    private final ValidatorChainBuilder validatorChainBuilder;
    private final RoleIdempotentValidator idempotentValidator;
    private final RoleBuiltInTypeValidator builtInTypeValidator;
    private final RoleOccupiedValidator occupiedValidator;

    @Override
    @Transactional(readOnly = true)
    public List<AclRoleVO> getAllRoles() {
        List<AclRolePO> roles = roleMapper.selectAll();
        return roles.stream()
                .map(po -> {
                    AclRoleVO vo = convertToVO(po);
                    // 查询绑定的菜单ID列表
                    List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(po.getId());
                    vo.setMenuIds(menuIds != null ? menuIds : new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AclRoleVO getRoleById(Long id) {
        if (id == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "角色ID不能为空");
        }

        AclRolePO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(AclErrorCode.ACL_5096, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5096));
        }

        AclRoleVO vo = convertToVO(role);
        
        // 查询绑定的菜单ID列表
        List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(id);
        vo.setMenuIds(menuIds != null ? menuIds : new ArrayList<>());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AclRoleVO createRole(AclRoleCreateDTO createDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildRoleCreateChain();
        ValidationResult validationResult = validator.validate(createDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_4002, validationResult.getErrorMessage());
        }

        // 创建角色
        AclRolePO role = new AclRolePO();
        role.setName(createDTO.getName().trim());
        role.setType(createDTO.getType());
        role.setStatus(AclStatus.ENABLED.getCode());
        role.setRemark(StringUtils.hasText(createDTO.getRemark()) ? createDTO.getRemark().trim() : null);

        Date now = new Date();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);

        int insertCount = roleMapper.insertRole(role);
        if (insertCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "创建角色失败");
        }

        // 设置幂等键（防止重复提交）
        idempotentValidator.setIdempotentKey(createDTO.getIdempotentKey());

        // 重新查询获取最新数据（包含生成的ID）
        AclRolePO insertedRole = roleMapper.selectById(role.getId());
        if (insertedRole == null) {
            throw new BusinessException(AclErrorCode.ACL_6002, "创建角色后查询失败");
        }

        log.info("创建角色成功: id={}, name={}", insertedRole.getId(), insertedRole.getName());
        return convertToVO(insertedRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AclRoleVO updateRole(AclRoleUpdateDTO updateDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildRoleUpdateChain();
        ValidationResult validationResult = validator.validate(updateDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_4002, validationResult.getErrorMessage());
        }

        Long roleId = Long.parseLong(updateDTO.getId());

        // 查询角色
        AclRolePO role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(AclErrorCode.ACL_5096, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5096));
        }

        // 更新角色
        role.setName(updateDTO.getName().trim());
        role.setType(updateDTO.getType());
        role.setStatus(updateDTO.getStatus());
        role.setRemark(StringUtils.hasText(updateDTO.getRemark()) ? updateDTO.getRemark().trim() : null);
        role.setUpdatedAt(new Date());

        int updateCount = roleMapper.updateRole(role);
        if (updateCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "更新角色失败");
        }

        // 重新查询获取最新数据
        AclRolePO updatedRole = roleMapper.selectById(roleId);
        if (updatedRole == null) {
            throw new BusinessException(AclErrorCode.ACL_6002, "更新角色后查询失败");
        }

        log.info("更新角色成功: id={}, name={}", updatedRole.getId(), updatedRole.getName());
        return convertToVO(updatedRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        if (id == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "角色ID不能为空");
        }

        // 查询角色
        AclRolePO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(AclErrorCode.ACL_5096, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5096));
        }

        // 检查是否为内置角色
        ValidationResult builtInResult = builtInTypeValidator.validateForDelete(id);
        if (!builtInResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_5097, builtInResult.getErrorMessage());
        }

        // 检查是否被用户占用
        ValidationResult occupiedResult = occupiedValidator.validate(id);
        if (!occupiedResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_5093, occupiedResult.getErrorMessage());
        }

        // 删除角色-菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(id);

        // 删除角色
        int deleteCount = roleMapper.deleteRole(id);
        if (deleteCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "删除角色失败");
        }

        log.info("删除角色成功: id={}, name={}", id, role.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRoleMenus(AclRoleMenuBindDTO bindDTO) {
        if (bindDTO == null || bindDTO.getRoleId() == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "角色ID不能为空");
        }

        Long roleId = Long.parseLong(bindDTO.getRoleId());

        // 验证角色是否存在
        AclRolePO role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(AclErrorCode.ACL_5096, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5096));
        }

        // 删除原有的角色-菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);

        // 批量插入新的角色-菜单关联
        List<Long> menuIds = bindDTO.getMenuIds();
        if (menuIds != null && !menuIds.isEmpty()) {
            int insertCount = roleMenuMapper.batchInsertRoleMenu(roleId, menuIds);
            if (insertCount != menuIds.size()) {
                throw new BusinessException(AclErrorCode.ACL_6001, "绑定角色-菜单关系失败");
            }
        }

        log.info("绑定角色-菜单关系成功: roleId={}, menuCount={}", roleId, 
                menuIds != null ? menuIds.size() : 0);
    }

    /**
     * 转换为VO
     */
    private AclRoleVO convertToVO(AclRolePO po) {
        AclRoleVO vo = new AclRoleVO();
        vo.setId(po.getId());
        vo.setName(po.getName());
        vo.setType(po.getType());
        vo.setStatus(po.getStatus());
        vo.setRemark(po.getRemark());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

