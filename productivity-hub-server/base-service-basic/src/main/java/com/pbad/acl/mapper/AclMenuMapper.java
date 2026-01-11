package com.pbad.acl.mapper;

import com.pbad.acl.domain.po.AclMenuPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ACL菜单 Mapper 接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclMenuMapper {

    /**
     * 查询所有菜单
     *
     * @return 菜单列表
     */
    List<AclMenuPO> selectAll();

    /**
     * 根据ID查询菜单
     *
     * @param id 菜单ID
     * @return 菜单
     */
    AclMenuPO selectById(@Param("id") Long id);

    /**
     * 根据父菜单ID查询子菜单列表
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    List<AclMenuPO> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有菜单（树形结构，按orderNum排序）
     *
     * @param includeHidden 是否包含隐藏菜单
     * @return 菜单列表
     */
    List<AclMenuPO> selectAllForTree(@Param("includeHidden") Boolean includeHidden);

    /**
     * 插入菜单
     *
     * @param menu 菜单
     * @return 插入行数
     */
    int insertMenu(AclMenuPO menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单
     * @return 更新行数
     */
    int updateMenu(AclMenuPO menu);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     * @return 删除行数
     */
    int deleteMenu(@Param("id") Long id);

    /**
     * 检查菜单是否被角色占用
     *
     * @param menuId 菜单ID
     * @return 占用数量
     */
    int countRoleMenuByMenuId(@Param("menuId") Long menuId);

    /**
     * 计算菜单层级深度
     *
     * @param id 菜单ID
     * @return 层级深度（从1开始）
     */
    int calculateMenuLevel(@Param("id") Long id);

    /**
     * 检查是否存在循环引用（通过检查父菜单的祖先是否包含当前菜单）
     *
     * @param id       当前菜单ID
     * @param parentId 父菜单ID
     * @return 是否存在循环引用（1-存在，0-不存在）
     */
    int checkCircularReference(@Param("id") Long id, @Param("parentId") Long parentId);

    /**
     * 根据菜单ID列表查询菜单（用于用户菜单权限查询）
     *
     * @param menuIds 菜单ID列表
     * @return 菜单列表
     */
    List<AclMenuPO> selectByIds(@Param("menuIds") List<Long> menuIds);
}

