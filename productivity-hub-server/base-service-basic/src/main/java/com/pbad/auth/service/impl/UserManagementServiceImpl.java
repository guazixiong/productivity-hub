package com.pbad.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.domain.vo.ManagedUserVO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserManagementService;
import com.pbad.config.service.ConfigService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户管理服务实现.
 */
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final ConfigService configService;
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(readOnly = true)
    public List<ManagedUserVO> listUsers() {
        List<UserPO> users = userMapper.selectAll();
        return users.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ManagedUserVO createUser(UserCreateDTO dto, String operator) {
        validateCreateRequest(dto);

        // 唯一性校验
        UserPO existing = userMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("400", "用户名已存在");
        }

        // 构造实体
        UserPO user = new UserPO();
        user.setId(idGeneratorApi.generateId());
        user.setUsername(dto.getUsername());
        user.setPassword(StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : DEFAULT_PASSWORD);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRoles(JSON.toJSONString(dto.getRoles()));

        // 插入用户
        int inserted = userMapper.insertUser(user);
        if (inserted <= 0) {
            throw new BusinessException("500", "创建用户失败");
        }

        // 按模板初始化用户配置
        configService.initializeUserConfigFromTemplate(user.getId(), operator);

        // 查询并返回
        UserPO saved = userMapper.selectById(user.getId());
        return toVO(saved);
    }

    private void validateCreateRequest(UserCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException("400", "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("400", "用户名不能为空");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new BusinessException("400", "姓名不能为空");
        }
        if (CollectionUtils.isEmpty(dto.getRoles())) {
            throw new BusinessException("400", "至少选择一个角色");
        }
    }

    private ManagedUserVO toVO(UserPO po) {
        ManagedUserVO vo = new ManagedUserVO();
        vo.setId(po.getId());
        vo.setUsername(po.getUsername());
        vo.setName(po.getName());
        vo.setEmail(po.getEmail());
        if (StringUtils.hasText(po.getRoles())) {
            try {
                vo.setRoles(JSON.parseArray(po.getRoles(), String.class));
            } catch (Exception ex) {
                vo.setRoles(Collections.emptyList());
            }
        }
        vo.setCreatedAt(formatDate(po.getCreatedAt()));
        vo.setUpdatedAt(formatDate(po.getUpdatedAt()));
        return vo;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}

