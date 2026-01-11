package com.pbad.acl.service.impl;

import com.pbad.acl.constants.AclErrorCode;
import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.domain.enums.AclStatus;
import com.pbad.acl.domain.po.AclMenuPO;
import com.pbad.acl.domain.vo.AclMenuTreeVO;
import com.pbad.acl.domain.vo.AclMenuVO;
import com.pbad.acl.mapper.AclMenuMapper;
import com.pbad.acl.service.AclMenuService;
import com.pbad.acl.validator.MenuIdempotentValidator;
import com.pbad.acl.validator.MenuOccupiedValidator;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ACL菜单服务实现类.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AclMenuServiceImpl implements AclMenuService {

    private final AclMenuMapper menuMapper;
    private final ValidatorChainBuilder validatorChainBuilder;
    private final MenuIdempotentValidator idempotentValidator;
    private final MenuOccupiedValidator occupiedValidator;

    @Override
    @Transactional(readOnly = true)
    public List<AclMenuTreeVO> getMenuTree(Boolean includeHidden) {
        // 查询所有菜单
        List<AclMenuPO> allMenus = menuMapper.selectAllForTree(includeHidden);

        // 转换为TreeVO
        List<AclMenuTreeVO> treeVOList = allMenus.stream()
                .map(this::convertToTreeVO)
                .collect(Collectors.toList());

        // 构建树形结构（组合模式）
        return buildMenuTree(treeVOList);
    }

    @Override
    @Transactional(readOnly = true)
    public AclMenuVO getMenuById(Long id) {
        if (id == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "菜单ID不能为空");
        }

        AclMenuPO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(AclErrorCode.ACL_5095, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5095));
        }

        return convertToVO(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AclMenuVO createMenu(AclMenuCreateDTO createDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildCreateChain();
        ValidationResult validationResult = validator.validate(createDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_4002, validationResult.getErrorMessage());
        }

        // 创建菜单
        AclMenuPO menu = new AclMenuPO();
        menu.setParentId(createDTO.getParentId());
        menu.setName(createDTO.getName().trim());
        menu.setPath(StringUtils.hasText(createDTO.getPath()) ? createDTO.getPath().trim() : null);
        menu.setComponent(StringUtils.hasText(createDTO.getComponent()) ? createDTO.getComponent().trim() : null);
        menu.setIcon(StringUtils.hasText(createDTO.getIcon()) ? createDTO.getIcon().trim() : null);
        menu.setType(createDTO.getType());
        menu.setVisible(createDTO.getVisible());
        menu.setOrderNum(createDTO.getOrderNum());
        menu.setStatus(AclStatus.ENABLED.getCode());

        Date now = new Date();
        menu.setCreatedAt(now);
        menu.setUpdatedAt(now);

        int insertCount = menuMapper.insertMenu(menu);
        if (insertCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "创建菜单失败");
        }

        // 设置幂等键（防止重复提交）
        idempotentValidator.setIdempotentKey(createDTO.getIdempotentKey());

        // 重新查询获取最新数据（包含生成的ID）
        AclMenuPO insertedMenu = menuMapper.selectById(menu.getId());
        if (insertedMenu == null) {
            throw new BusinessException(AclErrorCode.ACL_6002, "创建菜单后查询失败");
        }

        log.info("创建菜单成功: id={}, name={}", insertedMenu.getId(), insertedMenu.getName());
        return convertToVO(insertedMenu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AclMenuVO updateMenu(AclMenuUpdateDTO updateDTO) {
        // 使用责任链模式进行数据校验
        Validator validator = validatorChainBuilder.buildUpdateChain();
        ValidationResult validationResult = validator.validate(updateDTO);
        if (!validationResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_4002, validationResult.getErrorMessage());
        }

        Long menuId = Long.parseLong(updateDTO.getId());

        // 查询菜单
        AclMenuPO menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new BusinessException(AclErrorCode.ACL_5095, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5095));
        }

        // 更新菜单
        menu.setParentId(updateDTO.getParentId());
        menu.setName(updateDTO.getName().trim());
        menu.setPath(StringUtils.hasText(updateDTO.getPath()) ? updateDTO.getPath().trim() : null);
        menu.setComponent(StringUtils.hasText(updateDTO.getComponent()) ? updateDTO.getComponent().trim() : null);
        menu.setIcon(StringUtils.hasText(updateDTO.getIcon()) ? updateDTO.getIcon().trim() : null);
        menu.setType(updateDTO.getType());
        menu.setVisible(updateDTO.getVisible());
        menu.setOrderNum(updateDTO.getOrderNum());
        menu.setStatus(updateDTO.getStatus());
        menu.setUpdatedAt(new Date());

        int updateCount = menuMapper.updateMenu(menu);
        if (updateCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "更新菜单失败");
        }

        // 重新查询获取最新数据
        AclMenuPO updatedMenu = menuMapper.selectById(menuId);
        if (updatedMenu == null) {
            throw new BusinessException(AclErrorCode.ACL_6002, "更新菜单后查询失败");
        }

        log.info("更新菜单成功: id={}, name={}", updatedMenu.getId(), updatedMenu.getName());
        return convertToVO(updatedMenu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        if (id == null) {
            throw new BusinessException(AclErrorCode.ACL_4001, "菜单ID不能为空");
        }

        // 查询菜单
        AclMenuPO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(AclErrorCode.ACL_5095, AclErrorCode.getErrorMessage(AclErrorCode.ACL_5095));
        }

        // 检查是否有子菜单
        List<AclMenuPO> children = menuMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(AclErrorCode.ACL_5093, "无法删除菜单，存在子菜单");
        }

        // 检查是否被角色占用
        ValidationResult occupiedResult = occupiedValidator.validate(id);
        if (!occupiedResult.isValid()) {
            throw new BusinessException(AclErrorCode.ACL_5093, occupiedResult.getErrorMessage());
        }

        // 删除菜单
        int deleteCount = menuMapper.deleteMenu(id);
        if (deleteCount <= 0) {
            throw new BusinessException(AclErrorCode.ACL_6001, "删除菜单失败");
        }

        log.info("删除菜单成功: id={}, name={}", id, menu.getName());
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
                    // 父菜单不存在，作为根菜单处理
                    rootMenus.add(menu);
                }
            }
        }

        return rootMenus;
    }

    /**
     * 转换为VO
     */
    private AclMenuVO convertToVO(AclMenuPO po) {
        AclMenuVO vo = new AclMenuVO();
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

