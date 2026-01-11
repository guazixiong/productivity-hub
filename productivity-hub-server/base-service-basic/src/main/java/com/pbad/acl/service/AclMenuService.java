package com.pbad.acl.service;

import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.domain.vo.AclMenuTreeVO;
import com.pbad.acl.domain.vo.AclMenuVO;

import java.util.List;

/**
 * ACL菜单服务接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclMenuService {

    /**
     * 查询菜单树（可选包含隐藏菜单）
     *
     * @param includeHidden 是否包含隐藏菜单
     * @return 菜单树列表
     */
    List<AclMenuTreeVO> getMenuTree(Boolean includeHidden);

    /**
     * 根据ID获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    AclMenuVO getMenuById(Long id);

    /**
     * 创建菜单
     *
     * @param createDTO 创建DTO
     * @return 创建的菜单
     */
    AclMenuVO createMenu(AclMenuCreateDTO createDTO);

    /**
     * 更新菜单
     *
     * @param updateDTO 更新DTO
     * @return 更新后的菜单
     */
    AclMenuVO updateMenu(AclMenuUpdateDTO updateDTO);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void deleteMenu(Long id);
}

