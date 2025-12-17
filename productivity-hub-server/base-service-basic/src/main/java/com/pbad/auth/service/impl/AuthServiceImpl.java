package com.pbad.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.dto.LoginDTO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.domain.vo.LoginResponseVO;
import com.pbad.auth.domain.vo.ResetPasswordResponseVO;
import com.pbad.auth.domain.vo.UserVO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.AuthService;
import common.exception.BusinessException;
import common.util.JwtUtil;
import common.util.SpringCopyUtil;
import common.util.encryption.MD5Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    @Override
    @Transactional(readOnly = true)
    public LoginResponseVO login(LoginDTO loginDTO) {
        // 参数校验
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException("401", "用户名或密码不能为空");
        }

        // 查询用户
        UserPO userPO = userMapper.selectByUsername(loginDTO.getUsername());
        if (userPO == null) {
            throw new BusinessException("401", "账号或密码错误");
        }

        // 验证密码（注意：这里为了简化，使用明文比较。生产环境应该使用加密后的密码比较）
        // 由于初始化数据是明文，这里也使用明文比较
        if (!loginDTO.getPassword().equals(userPO.getPassword())) {
            throw new BusinessException("401", "账号或密码错误");
        }

        // 生成 Token（默认7天过期）
        String token = JwtUtil.generateToken(userPO.getId(), userPO.getUsername(), 7);
        String refreshToken = JwtUtil.generateToken(userPO.getId(), userPO.getUsername(), 30);

        // 构建用户信息
        UserVO userVO = new UserVO();
        userVO.setId(userPO.getId());
        userVO.setName(userPO.getName());
        userVO.setEmail(userPO.getEmail());
        // 解析角色 JSON 字符串
        if (userPO.getRoles() != null && !userPO.getRoles().isEmpty()) {
            try {
                List<String> roles = JSON.parseArray(userPO.getRoles(), String.class);
                userVO.setRoles(roles);
            } catch (Exception e) {
                log.warn("解析用户角色失败: {}", e.getMessage());
                userVO.setRoles(null);
            }
        }

        // 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(userVO);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResetPasswordResponseVO resetPassword(String userId) {
        // 参数校验
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException("400", "用户ID不能为空");
        }

        // 查询用户
        UserPO userPO = userMapper.selectById(userId);
        if (userPO == null) {
            throw new BusinessException("404", "用户不存在");
        }

        // 更新密码为默认密码（明文存储，生产环境应该加密）
        int updateCount = userMapper.updatePassword(userId, DEFAULT_PASSWORD);
        if (updateCount <= 0) {
            throw new BusinessException("500", "重置密码失败");
        }

        // 构建响应
        ResetPasswordResponseVO response = new ResetPasswordResponseVO();
        response.setSuccess(true);
        response.setMessage("密码已重置为默认密码 " + DEFAULT_PASSWORD);

        return response;
    }
}

