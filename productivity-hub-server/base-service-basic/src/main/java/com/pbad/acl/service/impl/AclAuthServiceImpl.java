package com.pbad.acl.service.impl;

import com.pbad.acl.domain.enums.AclStatus;
import com.pbad.acl.domain.enums.MenuVisible;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclMenuPO;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.domain.vo.AclMenuTreeVO;
import com.pbad.acl.mapper.AclMenuMapper;
import com.pbad.acl.mapper.AclRoleMenuMapper;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclUserRoleMapper;
import com.pbad.acl.service.AclAuthService;
import common.exception.BusinessException;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ACL认证服务实现类.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AclAuthServiceImpl implements AclAuthService {

    private final AclUserRoleMapper userRoleMapper;
    private final AclRoleMapper roleMapper;
    private final AclRoleMenuMapper roleMenuMapper;
    private final AclMenuMapper menuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AclMenuTreeVO> getCurrentUserMenus() {
        // 获取当前用户ID
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("401", "未登录或登录已过期");
        }

        // 查询用户绑定的角色ID列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            log.info("用户未绑定任何角色: userId={}", userId);
            return Collections.emptyList();
        }

        // 查询角色信息，检查是否有ADMIN角色（管理员拥有全部菜单）
        boolean isAdmin = false;
        Set<Long> menuIdSet = new HashSet<>();
        
        for (Long roleId : roleIds) {
            AclRolePO role = roleMapper.selectById(roleId);
            if (role != null && RoleType.ADMIN.getCode().equals(role.getType())) {
                isAdmin = true;
                break; // 管理员角色，直接拥有全部菜单
            }
            
            // 查询角色绑定的菜单ID列表
            List<Long> menuIds = roleMenuMapper.selectMenuIdsByRoleId(roleId);
            if (menuIds != null) {
                menuIdSet.addAll(menuIds);
            }
        }

        // 如果是管理员，查询所有启用的可见菜单
        List<AclMenuPO> menus;
        if (isAdmin) {
            menus = menuMapper.selectAllForTree(false); // 不包含隐藏菜单
        } else {
            // 普通用户，查询角色绑定的菜单（并集）
            if (menuIdSet.isEmpty()) {
                log.info("用户角色未绑定任何菜单: userId={}, roleIds={}", userId, roleIds);
                return Collections.emptyList();
            }
            menus = menuMapper.selectByIds(new ArrayList<>(menuIdSet));
        }

        // 过滤：只保留启用状态且可见的菜单
        List<AclMenuPO> filteredMenus = menus.stream()
                .filter(menu -> AclStatus.ENABLED.getCode().equals(menu.getStatus()))
                .filter(menu -> MenuVisible.VISIBLE.getCode() == menu.getVisible())
                .collect(Collectors.toList());

        // 转换为TreeVO
        List<AclMenuTreeVO> treeVOList = filteredMenus.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        // 构建树形结构（组合模式）
        List<AclMenuTreeVO> menuTree = buildMenuTree(treeVOList);

        log.info("获取用户菜单成功: userId={}, roleCount={}, menuCount={}, treeNodeCount={}", 
                userId, roleIds.size(), filteredMenus.size(), menuTree.size());
        
        return menuTree;
    }

    /**
     * 构建菜单树（组合模式）
     *
     * @param allMenus 所有菜单列表
     * @return 树形菜单列表
     */
    private List<AclMenuTreeVO> buildMenuTree(List<AclMenuTreeVO> allMenus) {
        // 使用Map存储所有菜单，key为菜单ID
        Map<Long, AclMenuTreeVO> menuMap = allMenus.stream()
                .collect(Collectors.toMap(AclMenuTreeVO::getId, menu -> menu));

        // 根菜单列表
        List<AclMenuTreeVO> rootMenus = new ArrayList<>();

        // 遍历所有菜单，构建树形结构
        for (AclMenuTreeVO menu : allMenus) {
            Long parentId = menu.getParentId();
            if (parentId == null) {
                // 根菜单
                rootMenus.add(menu);
            } else {
                // 子菜单，添加到父菜单的children中
                AclMenuTreeVO parent = menuMap.get(parentId);
                if (parent != null) {
                    parent.addChild(menu);
                } else {
                    // 父菜单不在当前用户的权限范围内，作为根菜单处理
                    rootMenus.add(menu);
                }
            }
        }

        // 按orderNum排序
        rootMenus.sort(Comparator.comparing(AclMenuTreeVO::getOrderNum, Comparator.nullsLast(Integer::compareTo)));
        sortChildren(rootMenus);

        return rootMenus;
    }

    /**
     * 递归排序子菜单
     */
    private void sortChildren(List<AclMenuTreeVO> menus) {
        for (AclMenuTreeVO menu : menus) {
            if (menu.hasChildren()) {
                menu.getChildren().sort(Comparator.comparing(AclMenuTreeVO::getOrderNum, Comparator.nullsLast(Integer::compareTo)));
                sortChildren(menu.getChildren());
            }
        }
    }

    /**
     * 转换为TreeVO
     */
    private AclMenuTreeVO convertToTreeVO(AclMenuPO po) {
        AclMenuTreeVO vo = new AclMenuTreeVO();
        vo.setId(po.getId());
        vo.setParentId(po.getParentId());
        vo.setName(po.getName());
        vo.setPath(po.getPath());
        vo.setComponent(po.getComponent());
        vo.setIcon(po.getIcon());
        vo.setType(po.getType());
        vo.setVisible(po.getVisible());
        vo.setOrderNum(po.getOrderNum());
        vo.setStatus(po.getStatus());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

