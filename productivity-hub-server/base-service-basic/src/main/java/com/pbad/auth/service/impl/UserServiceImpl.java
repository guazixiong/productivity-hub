package com.pbad.auth.service.impl;

import com.pbad.auth.domain.dto.UserProfileUpdateDTO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.domain.vo.UserVO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserService;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclUserRoleMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 用户服务实现类.
 *
 * @author: pbad
 * @date: 2025-12-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final AclUserRoleMapper aclUserRoleMapper;
    private final AclRoleMapper aclRoleMapper;

    @Override
    public UserVO getProfile(String userId) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(userPO);
    }

    @Override
    public UserVO updateProfile(String userId, UserProfileUpdateDTO dto) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new RuntimeException("用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(dto.getName())) {
            userPO.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            userPO.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            userPO.setAvatar(dto.getAvatar());
        }
        if (dto.getBio() != null) {
            userPO.setBio(dto.getBio());
        }
        if (dto.getPhone() != null) {
            userPO.setPhone(dto.getPhone());
        }
        if (dto.getGender() != null) {
            userPO.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            userPO.setBirthday(dto.getBirthday());
        }
        if (dto.getAddress() != null) {
            userPO.setAddress(dto.getAddress());
        }
        if (dto.getCompany() != null) {
            userPO.setCompany(dto.getCompany());
        }
        if (dto.getPosition() != null) {
            userPO.setPosition(dto.getPosition());
        }
        if (dto.getWebsite() != null) {
            userPO.setWebsite(dto.getWebsite());
        }

        int updated = userMapper.updateProfile(userPO);
        if (updated <= 0) {
            throw new RuntimeException("更新用户信息失败");
        }

        // 重新查询获取最新数据
        UserPO updatedUserPO = userMapper.selectById(userId);
        return convertToVO(updatedUserPO);
    }

    @Override
    public UserVO updateAvatar(String userId, String avatarUrl) {
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new RuntimeException("用户不存在");
        }

        userPO.setAvatar(avatarUrl);
        int updated = userMapper.updateProfile(userPO);
        if (updated <= 0) {
            throw new RuntimeException("更新头像失败");
        }

        // 重新查询获取最新数据
        UserPO updatedUserPO = userMapper.selectById(userId);
        return convertToVO(updatedUserPO);
    }

    /**
     * 将 PO 转换为 VO
     */
    private UserVO convertToVO(UserPO userPO) {
        UserVO userVO = new UserVO();
        userVO.setId(userPO.getId());
        userVO.setName(userPO.getName());
        userVO.setEmail(userPO.getEmail());
        userVO.setAvatar(userPO.getAvatar());
        userVO.setBio(userPO.getBio());
        userVO.setPhone(userPO.getPhone());
        userVO.setGender(userPO.getGender());
        userVO.setBirthday(userPO.getBirthday());
        userVO.setAddress(userPO.getAddress());
        userVO.setCompany(userPO.getCompany());
        userVO.setPosition(userPO.getPosition());
        userVO.setWebsite(userPO.getWebsite());
        userVO.setCreatedAt(userPO.getCreatedAt());
        userVO.setUpdatedAt(userPO.getUpdatedAt());

        // 解析角色列表
        List<String> roles = new ArrayList<>();
        if (StringUtils.hasText(userPO.getRoles())) {
            try {
                roles = objectMapper.readValue(userPO.getRoles(), new TypeReference<List<String>>() {});
                if (roles == null) {
                    roles = new ArrayList<>();
                }
            } catch (Exception e) {
                log.warn("解析用户角色失败: {}", userPO.getRoles(), e);
                roles = new ArrayList<>();
            }
        }
        
        // 检查用户是否通过ACL系统拥有管理员角色
        try {
            List<Long> roleIds = aclUserRoleMapper.selectRoleIdsByUserId(userPO.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    AclRolePO role = aclRoleMapper.selectById(roleId);
                    if (role != null && RoleType.ADMIN.getCode().equals(role.getType())) {
                        // 用户通过ACL系统拥有管理员角色，确保 roles 中包含 'admin'
                        if (!roles.contains("admin")) {
                            roles.add("admin");
                            log.debug("[UserService] 用户 {} 通过ACL系统拥有管理员角色，已添加到 roles 中", userPO.getId());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ACL系统检查失败不影响流程，只记录日志
            log.warn("[UserService] 检查用户 {} ACL角色失败: {}", userPO.getId(), e.getMessage());
        }
        
        userVO.setRoles(roles);

        return userVO;
    }
}

