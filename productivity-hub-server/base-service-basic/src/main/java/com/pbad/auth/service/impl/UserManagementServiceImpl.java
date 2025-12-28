package com.pbad.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.dto.UserCreateDTO;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.domain.vo.ManagedUserVO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.auth.service.UserManagementService;
import com.pbad.auth.util.UserRoleUtil;
import com.pbad.config.mapper.UserConfigMapper;
import com.pbad.config.service.ConfigService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final UserConfigMapper userConfigMapper;
    private final ConfigService configService;
    private final IdGeneratorApi idGeneratorApi;
    private final JdbcTemplate jdbcTemplate;
    private final UserRoleUtil userRoleUtil;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userId, String operator) {
        if (!StringUtils.hasText(userId)) {
            throw new BusinessException("400", "用户ID不能为空");
        }

        // 验证用户是否存在
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("404", "用户不存在");
        }

        // 禁止删除超级管理员
        if (userRoleUtil.isAdmin(userId)) {
            throw new BusinessException("400", "超级管理员用户禁止被删除");
        }

        log.info("开始删除用户及其所有相关数据，用户ID: {}, 操作人: {}", userId, operator);

        try {
            // 1. 删除用户配置
            userConfigMapper.deleteByUserId(userId);
            log.debug("已删除用户配置，用户ID: {}", userId);

            // 2. 删除通知
            jdbcTemplate.update("DELETE FROM sys_notification WHERE user_id = ?", userId);
            log.debug("已删除通知，用户ID: {}", userId);

            // 3. 删除图片
            jdbcTemplate.update("DELETE FROM sys_image WHERE user_id = ?", userId);
            log.debug("已删除图片，用户ID: {}", userId);

            // 4. 删除消息历史
            jdbcTemplate.update("DELETE FROM sys_message_history WHERE user_id = ?", userId);
            log.debug("已删除消息历史，用户ID: {}", userId);

            // 5. 删除书签URL标签关联（先删除关联关系）
            jdbcTemplate.update("DELETE FROM bookmark_url_tag WHERE url_id IN (SELECT id FROM bookmark_url WHERE user_id = ?)", userId);
            log.debug("已删除书签URL标签关联，用户ID: {}", userId);

            // 6. 删除书签URL
            jdbcTemplate.update("DELETE FROM bookmark_url WHERE user_id = ?", userId);
            log.debug("已删除书签URL，用户ID: {}", userId);

            // 7. 删除书签标签
            jdbcTemplate.update("DELETE FROM bookmark_tag WHERE user_id = ?", userId);
            log.debug("已删除书签标签，用户ID: {}", userId);

            // 8. 删除健康相关数据
            jdbcTemplate.update("DELETE FROM health_exercise_record WHERE user_id = ?", userId);
            log.debug("已删除运动记录，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM health_training_plan WHERE user_id = ?", userId);
            log.debug("已删除训练计划，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM health_water_intake WHERE user_id = ?", userId);
            log.debug("已删除饮水记录，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM health_water_target WHERE user_id = ?", userId);
            log.debug("已删除饮水目标，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM health_weight_record WHERE user_id = ?", userId);
            log.debug("已删除体重记录，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM health_user_body_info WHERE user_id = ?", userId);
            log.debug("已删除用户身体信息，用户ID: {}", userId);

            // 9. 删除待办相关数据
            jdbcTemplate.update("DELETE FROM todo_event WHERE user_id = ?", userId);
            log.debug("已删除待办事件，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM todo_task WHERE user_id = ?", userId);
            log.debug("已删除待办任务，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM todo_module WHERE user_id = ?", userId);
            log.debug("已删除待办模块，用户ID: {}", userId);

            // 10. 删除公告阅读记录
            jdbcTemplate.update("DELETE FROM announcement_read WHERE user_id = ?", userId);
            log.debug("已删除公告阅读记录，用户ID: {}", userId);

            // 11. 删除代码生成器相关数据
            jdbcTemplate.update("DELETE FROM code_generator_database_config WHERE created_by = ?", userId);
            log.debug("已删除数据库配置，用户ID: {}", userId);

            jdbcTemplate.update("DELETE FROM code_generator_company_template WHERE created_by = ?", userId);
            log.debug("已删除公司模板，用户ID: {}", userId);

            // 注意：sys_agent_log 和 sys_agent 表是系统级别的表，没有 user_id 字段，不需要删除

            // 12. 最后删除用户本身
            int deleted = userMapper.deleteById(userId);
            if (deleted <= 0) {
                throw new BusinessException("500", "删除用户失败");
            }
            log.info("用户及其所有相关数据已成功删除，用户ID: {}, 操作人: {}", userId, operator);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户数据时发生错误，用户ID: {}, 操作人: {}", userId, operator, e);
            throw new BusinessException("500", "删除用户数据失败: " + e.getMessage());
        }
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
}

