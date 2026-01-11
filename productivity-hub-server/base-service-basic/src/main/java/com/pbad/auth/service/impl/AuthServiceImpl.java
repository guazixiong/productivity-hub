package com.pbad.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.dto.LoginDTO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.domain.vo.CaptchaResponseVO;
import com.pbad.auth.domain.vo.LoginResponseVO;
import com.pbad.auth.domain.vo.ResetPasswordResponseVO;
import com.pbad.auth.domain.vo.UserVO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.AuthService;
import com.pbad.auth.util.CaptchaUtil;
import com.pbad.acl.domain.enums.RoleType;
import com.pbad.acl.domain.po.AclRolePO;
import com.pbad.acl.mapper.AclRoleMapper;
import com.pbad.acl.mapper.AclUserRoleMapper;
import com.pbad.cache.service.UserCacheService;
import common.exception.BusinessException;
import common.util.JwtUtil;
import common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RedisUtil redisUtil;
    private final UserCacheService userCacheService;
    private final AclUserRoleMapper aclUserRoleMapper;
    private final AclRoleMapper aclRoleMapper;

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 验证码Redis Key前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /**
     * 验证码过期时间（5分钟）
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    @Override
    public CaptchaResponseVO generateCaptcha() {
        // 生成验证码
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();

        // 生成唯一Key
        String captchaKey = UUID.randomUUID().toString().replace("-", "");

        // 存储到Redis（5分钟过期）
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        redisUtil.setKey(redisKey, captchaResult.getCode().toUpperCase(), CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 构建响应
        CaptchaResponseVO response = new CaptchaResponseVO();
        response.setKey(captchaKey);
        response.setImage(captchaResult.getImageBase64());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseVO login(LoginDTO loginDTO) {
        // 参数校验
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BusinessException("401", "用户名或密码不能为空");
        }

        // 验证码校验
        if (!StringUtils.hasText(loginDTO.getCaptcha()) || !StringUtils.hasText(loginDTO.getCaptchaKey())) {
            throw new BusinessException("401", "请输入验证码");
        }

        String redisKey = CAPTCHA_KEY_PREFIX + loginDTO.getCaptchaKey();
        Object storedCaptcha = redisUtil.getValue(redisKey);
        if (storedCaptcha == null) {
            throw new BusinessException("401", "验证码已过期，请刷新后重试");
        }

        String expectedCaptcha = storedCaptcha.toString().toUpperCase();
        String inputCaptcha = loginDTO.getCaptcha().toUpperCase();
        if (!expectedCaptcha.equals(inputCaptcha)) {
            throw new BusinessException("401", "验证码错误");
        }

        // 验证通过后删除验证码（一次性使用）
        redisUtil.delete(redisKey);

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
        userVO.setAvatar(userPO.getAvatar());
        userVO.setBio(userPO.getBio());
        userVO.setPhone(userPO.getPhone());
        
        // 解析角色 JSON 字符串
        List<String> roles = new ArrayList<>();
        if (userPO.getRoles() != null && !userPO.getRoles().isEmpty()) {
            try {
                roles = JSON.parseArray(userPO.getRoles(), String.class);
                if (roles == null) {
                    roles = new ArrayList<>();
                }
            } catch (Exception e) {
                log.warn("解析用户角色失败: {}", e.getMessage());
                roles = new ArrayList<>();
            }
        }
        
        // 检查用户是否通过ACL系统拥有管理员角色
        try {
            List<Long> roleIds = aclUserRoleMapper.selectRoleIdsByUserId(userPO.getId());
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    AclRolePO role = aclRoleMapper.selectById(roleId);
                    if (role != null && RoleType.ADMIN.getCode().equals(role.getType())) {
                        // 用户通过ACL系统拥有管理员角色，确保 roles 中包含 'admin'
                        if (!roles.contains("admin")) {
                            roles.add("admin");
                            log.debug("[Auth] 用户 {} 通过ACL系统拥有管理员角色，已添加到 roles 中", userPO.getId());
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ACL系统检查失败不影响登录流程，只记录日志
            log.warn("[Auth] 检查用户 {} ACL角色失败: {}", userPO.getId(), e.getMessage());
        }
        
        userVO.setRoles(roles);

        // 构建响应
        LoginResponseVO response = new LoginResponseVO();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUser(userVO);

        // 登录成功后，先清理旧缓存（如果存在），再异步加载新缓存
        // 这样可以确保缓存的一致性，避免使用过期数据
        try {
            // 先清理可能存在的旧缓存
            userCacheService.clearUserCache(userPO.getId());
            // 然后异步加载新缓存
            userCacheService.loadUserCache(userPO.getId());
            log.debug("[Auth] 已触发用户 {} 的缓存清理和加载", userPO.getId());
        } catch (Exception e) {
            // 缓存操作失败不影响登录流程，只记录日志
            log.warn("[Auth] 用户 {} 缓存操作失败: {}", userPO.getId(), e.getMessage(), e);
        }

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

    @Override
    public void logout(String userId) {
        if (userId == null || userId.isEmpty()) {
            log.warn("[Auth] 用户ID为空，跳过缓存清理");
            return;
        }

        try {
            userCacheService.clearUserCache(userId);
            log.info("[Auth] 用户 {} 退出登录，缓存已清理", userId);
        } catch (Exception e) {
            // 缓存清理失败不影响退出流程，只记录日志
            log.warn("[Auth] 用户 {} 退出登录时缓存清理失败: {}", userId, e.getMessage(), e);
        }
    }
}

