package com.pbad.acl.service.impl;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclUserRoleBindDTO;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclUserRoleMapper;
import com.pbad.acl.service.AclUserRoleService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ACL用户-角色服务实现类.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AclUserRoleServiceImpl implements AclUserRoleService {

    private final AclUserRoleMapper userRoleMapper;
    private final AclRoleMapper roleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserRoles(AclUserRoleBindDTO bindDTO) {
        if (bindDTO == null || !StringUtils.hasText(bindDTO.getUserId())) {
            throw new BusinessException(AclErrorCode.ACL_4001, "用户ID不能为空");
        }

        String userId = bindDTO.getUserId();
        List<Long> roleIds = bindDTO.getRoleIds();

        // 验证角色是否存在
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AclRolePO role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new BusinessException(AclErrorCode.ACL_5096, 
                            String.format("角色不存在: roleId=%d", roleId));
                }
            }
        }

        // 删除原有的用户-角色关联
        userRoleMapper.deleteUserRoleByUserId(userId);

        // 批量插入新的用户-角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            int insertCount = userRoleMapper.batchInsertUserRole(userId, roleIds);
            if (insertCount != roleIds.size()) {
                throw new BusinessException(AclErrorCode.ACL_6001, "绑定用户-角色关系失败");
            }
        }

        log.info("绑定用户-角色关系成功: userId={}, roleCount={}", userId, 
                roleIds != null ? roleIds.size() : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRoleIdsByUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException(AclErrorCode.ACL_4001, "用户ID不能为空");
        }
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "角色ID不能为空");
        }
        return userRoleMapper.selectUserIdsByRoleId(roleId);
    }
}

