package com.pbad.auth.service;

import com.pbad.auth.domain.dto.UserProfileUpdateDTO;
import com.pbad.auth.domain.vo.UserVO;

/**
 * 用户服务接口.
 *
 * @author: pbad
 * @date: 2025-12-XX
 * @version: 1.0
 */
public interface UserService {
    /**
     * 获取当前用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getProfile(String userId);

    /**
     * 更新用户个人信息
     *
     * @param userId 用户ID
     * @param dto    更新数据
     * @return 更新后的用户信息
     */
    UserVO updateProfile(String userId, UserProfileUpdateDTO dto);

    /**
     * 上传用户头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像URL
     * @return 更新后的用户信息
     */
    UserVO updateAvatar(String userId, String avatarUrl);
}

