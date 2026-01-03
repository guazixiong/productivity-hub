package com.pbad.basic.auth.controller;

import com.pbad.auth.domain.dto.UserProfileUpdateDTO;
import com.pbad.auth.domain.vo.UserVO;
import com.pbad.auth.service.UserService;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.service.ImageService;
import common.core.domain.ApiResponse;
import common.web.context.RequestUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人信息控制器.
 *
 * @author: pbad
 * @date: 2025-12-XX
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<UserVO> getProfile() {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        try {
            UserVO userVO = userService.getProfile(userId);
            return ApiResponse.ok(userVO);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ApiResponse.fail(500, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户个人信息
     *
     * @param dto 更新数据
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@RequestBody UserProfileUpdateDTO dto) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }
        try {
            UserVO userVO = userService.updateProfile(userId, dto);
            return ApiResponse.ok("更新成功", userVO);
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return ApiResponse.fail(500, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     * 使用全局图片管理服务进行上传
     *
     * @param file 头像文件
     * @return 更新后的用户信息（包含头像URL）
     */
    @PostMapping("/avatar")
    public ApiResponse<UserVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String userId = RequestUserContext.getUserId();
        if (!StringUtils.hasText(userId)) {
            return ApiResponse.unauthorized("未登录或登录已过期");
        }

        // 验证文件
        if (file == null || file.isEmpty()) {
            return ApiResponse.fail(400, "请选择要上传的文件");
        }

        try {
            // 使用全局图片管理服务上传头像
            // category 设置为 "avatar"，businessModule 设置为 "user"，businessId 设置为 userId
            ImageVO imageVO = imageService.uploadImage(
                    file,
                    "avatar",           // category
                    "user",            // businessModule
                    userId,            // businessId
                    "用户头像",         // description
                    userId             // userId
            );

            // 使用上传后的图片URL更新用户头像
            String avatarUrl = imageVO.getFileUrl();
            UserVO userVO = userService.updateAvatar(userId, avatarUrl);

            return ApiResponse.ok("头像上传成功", userVO);
        } catch (Exception e) {
            log.error("上传头像失败", e);
            return ApiResponse.fail(500, "上传头像失败: " + e.getMessage());
        }
    }
}

