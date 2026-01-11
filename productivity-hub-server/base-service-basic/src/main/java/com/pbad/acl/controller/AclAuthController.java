package com.pbad.acl.controller;

import com.pbad.acl.domain.vo.AclMenuTreeVO;
import com.pbad.acl.service.AclAuthService;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ACL认证控制器.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/acl/auth")
@RequiredArgsConstructor
public class AclAuthController {

    private final AclAuthService authService;

    /**
     * 获取当前登录用户的菜单树（返回并集+显隐过滤）
     * API-REQ-004-01: GET /acl/auth/menus
     *
     * @return 菜单树列表（已过滤可见性）
     */
    @GetMapping("/menus")
    public ApiResponse<List<AclMenuTreeVO>> getCurrentUserMenus() {
        List<AclMenuTreeVO> menus = authService.getCurrentUserMenus();
        return ApiResponse.ok(menus);
    }
}

