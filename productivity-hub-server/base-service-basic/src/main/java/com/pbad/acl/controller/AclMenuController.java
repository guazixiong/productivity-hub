package com.pbad.acl.controller;

import com.pbad.acl.domain.dto.AclMenuCreateDTO;
import com.pbad.acl.domain.dto.AclMenuUpdateDTO;
import com.pbad.acl.domain.vo.AclMenuTreeVO;
import com.pbad.acl.domain.vo.AclMenuVO;
import com.pbad.acl.service.AclMenuService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * ACL菜单控制器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/acl/menus")
@RequiredArgsConstructor
@Validated
public class AclMenuController {

    private final AclMenuService menuService;

    /**
     * 查询菜单树（可选包含隐藏菜单）
     * API-REQ-001-01: GET /acl/menus/tree
     *
     * @param includeHidden 是否包含隐藏菜单（可选，默认false）
     * @return 菜单树列表
     */
    @GetMapping("/tree")
    public ApiResponse<List<AclMenuTreeVO>> getMenuTree(
            @RequestParam(required = false, defaultValue = "false") Boolean includeHidden) {
        List<AclMenuTreeVO> menuTree = menuService.getMenuTree(includeHidden);
        return ApiResponse.ok(menuTree);
    }

    /**
     * 根据ID获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/detail")
    public ApiResponse<AclMenuVO> getMenuById(@RequestParam Long id) {
        AclMenuVO menu = menuService.getMenuById(id);
        return ApiResponse.ok(menu);
    }

    /**
     * 创建菜单
     * API-REQ-001-02: POST /acl/menus/create
     *
     * @param createDTO 创建DTO
     * @return 创建的菜单
     */
    @PostMapping("/create")
    public ApiResponse<AclMenuVO> createMenu(@Valid @RequestBody AclMenuCreateDTO createDTO) {
        AclMenuVO menu = menuService.createMenu(createDTO);
        return ApiResponse.ok(menu);
    }

    /**
     * 更新菜单
     * API-REQ-001-02: POST /acl/menus/update
     *
     * @param updateDTO 更新DTO
     * @return 更新后的菜单
     */
    @PostMapping("/update")
    public ApiResponse<AclMenuVO> updateMenu(@Valid @RequestBody AclMenuUpdateDTO updateDTO) {
        AclMenuVO menu = menuService.updateMenu(updateDTO);
        return ApiResponse.ok(menu);
    }

    /**
     * 删除菜单
     * API-REQ-001-02: POST /acl/menus/delete
     *
     * @param id 菜单ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ApiResponse<Void> deleteMenu(@RequestParam Long id) {
        menuService.deleteMenu(id);
        return ApiResponse.ok(null);
    }
}

