package com.pbad.acl.service;

import com.pbad.acl.domain.vo.AclMenuTreeVO;

import java.util.List;

/**
 * ACL认证服务接口.
 *
 * @author: pbad
 * @date: 2026-01-07
 * @version: 1.0
 */
public interface AclAuthService {

    /**
     * 获取当前登录用户的菜单树（返回并集+显隐过滤）
     * API-REQ-004-01: GET /acl/auth/menus
     *
     * @return 菜单树列表（已过滤可见性）
     */
    List<AclMenuTreeVO> getCurrentUserMenus();
}

