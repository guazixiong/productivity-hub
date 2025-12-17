package com.pbad.config.service.impl;

import com.alibaba.fastjson.JSON;
import com.pbad.auth.domain.po.UserPO;
import com.pbad.auth.mapper.UserMapper;
import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.po.ConfigItemPO;
import com.pbad.config.domain.vo.ConfigItemVO;
import com.pbad.config.domain.po.UserConfigPO;
import com.pbad.config.mapper.ConfigMapper;
import com.pbad.config.mapper.UserConfigMapper;
import com.pbad.config.service.ConfigService;
import com.pbad.generator.api.IdGeneratorApi;
import common.util.RedisUtil;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 配置服务实现类.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;
    private final UserConfigMapper userConfigMapper;
    private final UserMapper userMapper;
    private final IdGeneratorApi idGeneratorApi;
    private final RedisUtil redisUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String TEMPLATE_USER_ID = "system";
    private static final String ADMIN_ROLE = "admin";
    private static final String USER_CONFIG_CACHE_KEY = "phub:config:list:%s";

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItemVO> getConfigList(String userId) {
        validateUserId(userId);
        String cacheKey = buildCacheKey(userId);
        Object cacheHit = redisUtil.getValue(cacheKey);
        if (cacheHit instanceof List) {
            @SuppressWarnings("unchecked")
            List<ConfigItemVO> cachedList = (List<ConfigItemVO>) cacheHit;
            if (!cachedList.isEmpty()) {
                return cachedList;
            }
        }

        List<ConfigItemVO> configList = loadConfigListFromDb(userId);
        redisUtil.defaultSetKey(cacheKey, configList);
        return configList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigItemVO updateConfig(ConfigUpdateDTO updateDTO, String userId, String updatedBy) {
        // 参数校验
        if (updateDTO == null || updateDTO.getId() == null || updateDTO.getId().isEmpty()) {
            throw new BusinessException("400", "配置ID不能为空");
        }
        validateUserId(userId);

        if (isAdminUser(userId)) {
            ConfigItemPO templatePO = configMapper.selectById(updateDTO.getId());
            if (templatePO == null) {
                throw new BusinessException("404", "配置项不存在");
            }
            templatePO.setConfigValue(updateDTO.getValue());
            if (updateDTO.getDescription() != null && !updateDTO.getDescription().isEmpty()) {
                templatePO.setDescription(updateDTO.getDescription());
            }
            templatePO.setUpdatedBy(updatedBy);
            int updateCount = configMapper.updateConfig(templatePO);
            if (updateCount <= 0) {
                throw new BusinessException("500", "更新配置失败");
            }
            ConfigItemPO updated = configMapper.selectById(updateDTO.getId());
            refreshUserConfigCache(userId);
            return convertTemplateToVO(updated);
        }

        // 查询配置项
        UserConfigPO configPO = userConfigMapper.selectById(updateDTO.getId());
        if (configPO == null) {
            throw new BusinessException("404", "配置项不存在");
        }
        if (!userId.equals(configPO.getUserId())) {
            throw new BusinessException("403", "无权修改其他用户配置");
        }

        // 更新配置值
        // 为防止外部传入的 PO 丢失 userId，显式回填当前用户，确保更新语句只作用于该用户
        configPO.setUserId(userId);
        configPO.setConfigValue(updateDTO.getValue());
        if (updateDTO.getDescription() != null && !updateDTO.getDescription().isEmpty()) {
            configPO.setDescription(updateDTO.getDescription());
        }
        configPO.setUpdatedBy(updatedBy);

        // 执行更新
        int updateCount = userConfigMapper.updateConfig(configPO);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新配置失败");
        }

        // 重新查询更新后的数据
        UserConfigPO updatedPO = userConfigMapper.selectById(updateDTO.getId());
        refreshUserConfigCache(userId);
        return convertToVO(updatedPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigItemVO createOrUpdateConfig(ConfigCreateOrUpdateDTO createOrUpdateDTO, String userId, String updatedBy) {
        // 参数校验
        if (createOrUpdateDTO == null || createOrUpdateDTO.getModule() == null || createOrUpdateDTO.getModule().isEmpty()
                || createOrUpdateDTO.getKey() == null || createOrUpdateDTO.getKey().isEmpty()) {
            throw new BusinessException("400", "模块名和配置键不能为空");
        }
        validateUserId(userId);

        if (isAdminUser(userId)) {
            ConfigItemPO existingTemplate = configMapper.selectByModuleAndKey(createOrUpdateDTO.getModule(), createOrUpdateDTO.getKey());
            if (existingTemplate != null) {
                existingTemplate.setConfigValue(createOrUpdateDTO.getValue());
                if (createOrUpdateDTO.getDescription() != null && !createOrUpdateDTO.getDescription().isEmpty()) {
                    existingTemplate.setDescription(createOrUpdateDTO.getDescription());
                }
                existingTemplate.setUpdatedBy(updatedBy);
                int updateCount = configMapper.updateConfig(existingTemplate);
                if (updateCount <= 0) {
                    throw new BusinessException("500", "更新配置失败");
                }
                ConfigItemPO updated = configMapper.selectById(existingTemplate.getId());
                refreshUserConfigCache(userId);
                return convertTemplateToVO(updated);
            } else {
                ConfigItemPO newTemplate = new ConfigItemPO();
                newTemplate.setId(idGeneratorApi.generateId());
                newTemplate.setModule(createOrUpdateDTO.getModule());
                newTemplate.setConfigKey(createOrUpdateDTO.getKey());
                newTemplate.setConfigValue(createOrUpdateDTO.getValue());
                newTemplate.setDescription(createOrUpdateDTO.getDescription());
                newTemplate.setUpdatedBy(updatedBy);
                LocalDateTime now = LocalDateTime.now();
                String nowStr = now.format(DATE_TIME_FORMATTER);
                newTemplate.setCreatedAt(nowStr);
                newTemplate.setUpdatedAt(nowStr);
                int insertCount = configMapper.insertConfig(newTemplate);
                if (insertCount <= 0) {
                    throw new BusinessException("500", "创建配置失败");
                }
                ConfigItemPO inserted = configMapper.selectById(newTemplate.getId());
                refreshUserConfigCache(userId);
                return convertTemplateToVO(inserted);
            }
        }

        // 查询是否已存在
        UserConfigPO existingPO = userConfigMapper.selectByUserAndKey(userId, createOrUpdateDTO.getModule(), createOrUpdateDTO.getKey());

        if (existingPO != null) {
            // 存在则更新
            // 再次确保 userId 绑定当前用户，避免误更新其他用户配置
            existingPO.setUserId(userId);
            existingPO.setConfigValue(createOrUpdateDTO.getValue());
            if (createOrUpdateDTO.getDescription() != null && !createOrUpdateDTO.getDescription().isEmpty()) {
                existingPO.setDescription(createOrUpdateDTO.getDescription());
            }
            existingPO.setUpdatedBy(updatedBy);
            int updateCount = userConfigMapper.updateConfig(existingPO);
            if (updateCount <= 0) {
                throw new BusinessException("500", "更新配置失败");
            }
            UserConfigPO updatedPO = userConfigMapper.selectById(existingPO.getId());
            refreshUserConfigCache(userId);
            return convertToVO(updatedPO);
        } else {
            // 不存在则创建
            UserConfigPO newPO = new UserConfigPO();
            newPO.setId(idGeneratorApi.generateId());
            newPO.setUserId(userId);
            newPO.setModule(createOrUpdateDTO.getModule());
            newPO.setConfigKey(createOrUpdateDTO.getKey());
            newPO.setConfigValue(createOrUpdateDTO.getValue());
            newPO.setDescription(createOrUpdateDTO.getDescription());
            newPO.setUpdatedBy(updatedBy);
            LocalDateTime now = LocalDateTime.now();
            String nowStr = now.format(DATE_TIME_FORMATTER);
            newPO.setCreatedAt(nowStr);
            newPO.setUpdatedAt(nowStr);
            int insertCount = userConfigMapper.batchInsert(Collections.singletonList(newPO));
            if (insertCount <= 0) {
                throw new BusinessException("500", "创建配置失败");
            }
            UserConfigPO insertedPO = userConfigMapper.selectById(newPO.getId());
            refreshUserConfigCache(userId);
            return convertToVO(insertedPO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String module, String key, String userId) {
        if (module == null || module.isEmpty() || key == null || key.isEmpty()) {
            throw new BusinessException("400", "模块名和配置键不能为空");
        }
        if (isTemplateUser(userId)) {
            return getTemplateConfigValue(module, key);
        }
        validateUserId(userId);
        UserConfigPO configPO = userConfigMapper.selectByUserAndKey(userId, module, key);
        if (configPO == null) {
            throw new BusinessException("404", "配置项不存在: " + module + "." + key);
        }
        return configPO.getConfigValue();
    }

    @Override
    @Transactional(readOnly = true)
    public String getTemplateConfigValue(String module, String key) {
        if (module == null || module.isEmpty() || key == null || key.isEmpty()) {
            throw new BusinessException("400", "模块名和配置键不能为空");
        }
        ConfigItemPO configPO = configMapper.selectByModuleAndKey(module, key);
        if (configPO == null) {
            throw new BusinessException("404", "模板配置项不存在: " + module + "." + key);
        }
        return configPO.getConfigValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initializeUserConfigFromTemplate(String userId, String operator) {
        validateUserId(userId);
        ensureUserConfigs(userId, operator == null ? "system" : operator);
        refreshUserConfigCache(userId);
    }

    /**
     * 转换为 VO
     */
    private ConfigItemVO convertToVO(UserConfigPO po) {
        ConfigItemVO vo = new ConfigItemVO();
        vo.setId(po.getId());
        vo.setModule(po.getModule());
        vo.setKey(po.getConfigKey());
        vo.setValue(po.getConfigValue());
        vo.setDescription(po.getDescription());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        vo.setUpdatedBy(po.getUpdatedBy());
        return vo;
    }

    private ConfigItemVO convertTemplateToVO(ConfigItemPO po) {
        ConfigItemVO vo = new ConfigItemVO();
        vo.setId(po.getId());
        vo.setModule(po.getModule());
        vo.setKey(po.getConfigKey());
        vo.setValue(po.getConfigValue());
        vo.setDescription(po.getDescription());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        vo.setUpdatedBy(po.getUpdatedBy());
        return vo;
    }

    private void ensureUserConfigs(String userId, String operator) {
        int count = userConfigMapper.countByUserId(userId);
        if (count > 0) {
            return;
        }
        List<ConfigItemPO> templates = configMapper.selectAll();
        LocalDateTime now = LocalDateTime.now();
        String nowStr = now.format(DATE_TIME_FORMATTER);
        List<UserConfigPO> userConfigs = templates.stream().map(template -> {
            UserConfigPO po = new UserConfigPO();
            po.setId(idGeneratorApi.generateId());
            po.setUserId(userId);
            po.setModule(template.getModule());
            po.setConfigKey(template.getConfigKey());
            po.setConfigValue(template.getConfigValue());
            po.setDescription(template.getDescription());
            po.setCreatedAt(nowStr);
            po.setUpdatedAt(nowStr);
            po.setUpdatedBy(operator);
            return po;
        }).collect(Collectors.toList());
        if (!userConfigs.isEmpty()) {
            userConfigMapper.batchInsert(userConfigs);
        }
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException("401", "用户未登录或未提供用户ID");
        }
    }

    private boolean isAdminUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        UserPO user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            return true;
        }
        String roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        try {
            List<String> roleList = JSON.parseArray(roles, String.class);
            return roleList.stream().anyMatch(role -> ADMIN_ROLE.equalsIgnoreCase(role));
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTemplateUser(String userId) {
        return TEMPLATE_USER_ID.equals(userId);
    }

    private String buildCacheKey(String userId) {
        return String.format(USER_CONFIG_CACHE_KEY, userId);
    }

    private List<ConfigItemVO> loadConfigListFromDb(String userId) {
        if (isAdminUser(userId)) {
            List<ConfigItemPO> templateList = configMapper.selectAll();
            return templateList.stream().map(this::convertTemplateToVO).collect(Collectors.toList());
        }
        ensureUserConfigs(userId, "system");
        List<UserConfigPO> poList = userConfigMapper.selectByUserId(userId);
        return poList.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private void refreshUserConfigCache(String userId) {
        List<ConfigItemVO> latestList = loadConfigListFromDb(userId);
        redisUtil.defaultSetKey(buildCacheKey(userId), latestList);
    }
}

