package com.pbad.acl.controller;

import com.pbad.acl.domain.dto.AclRoleCreateDTO;
import com.pbad.acl.domain.dto.AclRoleMenuBindDTO;
import com.pbad.acl.domain.dto.AclRoleUpdateDTO;
import com.pbad.acl.domain.vo.AclRoleVO;
import com.pbad.acl.service.AclRoleService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * ACL角色控制器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/acl/roles")
@RequiredArgsConstructor
@Validated
public class AclRoleController {

    private final AclRoleService roleService;

    /**
     * 查询所有角色列表
     * API-REQ-002-01: GET /acl/roles/list
     *
     * @return 角色列表
     */
    @GetMapping("/list")
    public ApiResponse<List<AclRoleVO>> getAllRoles() {
        List<AclRoleVO> roles = roleService.getAllRoles();
        return ApiResponse.ok(roles);
    }

    /**
     * 根据ID获取角色详情（包含绑定的菜单ID列表）
     * API-REQ-002-01: GET /acl/roles/detail
     *
     * @param id 角色ID
     * @return 角色详情
     */
    @GetMapping("/detail")
    public ApiResponse<AclRoleVO> getRoleById(@RequestParam Long id) {
        AclRoleVO role = roleService.getRoleById(id);
        return ApiResponse.ok(role);
    }

    /**
     * 创建角色
     * API-REQ-002-02: POST /acl/roles/create
     *
     * @param createDTO 创建DTO
     * @return 创建的角色
     */
    @PostMapping("/create")
    public ApiResponse<AclRoleVO> createRole(@Valid @RequestBody AclRoleCreateDTO createDTO) {
        AclRoleVO role = roleService.createRole(createDTO);
        return ApiResponse.ok(role);
    }

    /**
     * 更新角色
     * API-REQ-002-02: POST /acl/roles/update
     *
     * @param updateDTO 更新DTO
     * @return 更新后的角色
     */
    @PostMapping("/update")
    public ApiResponse<AclRoleVO> updateRole(@Valid @RequestBody AclRoleUpdateDTO updateDTO) {
        AclRoleVO role = roleService.updateRole(updateDTO);
        return ApiResponse.ok(role);
    }

    /**
     * 删除角色
     * API-REQ-002-02: POST /acl/roles/delete
     *
     * @param id 角色ID
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ApiResponse<Void> deleteRole(@RequestParam Long id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(null);
    }

    /**
     * 绑定角色-菜单关系（覆盖式绑定）
     * API-REQ-002-03: POST /acl/roles/bind-menus
     *
     * @param bindDTO 绑定DTO
     * @return 绑定结果
     */
    @PostMapping("/bind-menus")
    public ApiResponse<Void> bindRoleMenus(@Valid @RequestBody AclRoleMenuBindDTO bindDTO) {
        roleService.bindRoleMenus(bindDTO);
        return ApiResponse.ok(null);
    }
}

