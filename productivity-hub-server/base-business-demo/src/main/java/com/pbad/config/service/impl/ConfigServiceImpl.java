package com.pbad.config.service.impl;

import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.po.ConfigItemPO;
import com.pbad.config.domain.vo.ConfigItemVO;
import com.pbad.config.mapper.ConfigMapper;
import com.pbad.config.service.ConfigService;
import com.pbad.generator.api.IdGeneratorApi;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(readOnly = true)
    public List<ConfigItemVO> getConfigList() {
        List<ConfigItemPO> poList = configMapper.selectAll();
        return poList.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigItemVO updateConfig(ConfigUpdateDTO updateDTO, String updatedBy) {
        // 参数校验
        if (updateDTO == null || updateDTO.getId() == null || updateDTO.getId().isEmpty()) {
            throw new BusinessException("400", "配置ID不能为空");
        }

        // 查询配置项
        ConfigItemPO configPO = configMapper.selectById(updateDTO.getId());
        if (configPO == null) {
            throw new BusinessException("404", "配置项不存在");
        }

        // 更新配置值
        configPO.setConfigValue(updateDTO.getValue());
        if (updateDTO.getDescription() != null && !updateDTO.getDescription().isEmpty()) {
            configPO.setDescription(updateDTO.getDescription());
        }
        configPO.setUpdatedBy(updatedBy);

        // 执行更新
        int updateCount = configMapper.updateConfig(configPO);
        if (updateCount <= 0) {
            throw new BusinessException("500", "更新配置失败");
        }

        // 重新查询更新后的数据
        ConfigItemPO updatedPO = configMapper.selectById(updateDTO.getId());
        return convertToVO(updatedPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigItemVO createOrUpdateConfig(ConfigCreateOrUpdateDTO createOrUpdateDTO, String updatedBy) {
        // 参数校验
        if (createOrUpdateDTO == null || createOrUpdateDTO.getModule() == null || createOrUpdateDTO.getModule().isEmpty()
                || createOrUpdateDTO.getKey() == null || createOrUpdateDTO.getKey().isEmpty()) {
            throw new BusinessException("400", "模块名和配置键不能为空");
        }

        // 查询是否已存在
        ConfigItemPO existingPO = configMapper.selectByModuleAndKey(createOrUpdateDTO.getModule(), createOrUpdateDTO.getKey());

        if (existingPO != null) {
            // 存在则更新
            existingPO.setConfigValue(createOrUpdateDTO.getValue());
            if (createOrUpdateDTO.getDescription() != null && !createOrUpdateDTO.getDescription().isEmpty()) {
                existingPO.setDescription(createOrUpdateDTO.getDescription());
            }
            existingPO.setUpdatedBy(updatedBy);
            int updateCount = configMapper.updateConfig(existingPO);
            if (updateCount <= 0) {
                throw new BusinessException("500", "更新配置失败");
            }
            ConfigItemPO updatedPO = configMapper.selectById(existingPO.getId());
            return convertToVO(updatedPO);
        } else {
            // 不存在则创建
            ConfigItemPO newPO = new ConfigItemPO();
            newPO.setId(idGeneratorApi.generateId());
            newPO.setModule(createOrUpdateDTO.getModule());
            newPO.setConfigKey(createOrUpdateDTO.getKey());
            newPO.setConfigValue(createOrUpdateDTO.getValue());
            newPO.setDescription(createOrUpdateDTO.getDescription());
            newPO.setUpdatedBy(updatedBy);
            int insertCount = configMapper.insertConfig(newPO);
            if (insertCount <= 0) {
                throw new BusinessException("500", "创建配置失败");
            }
            ConfigItemPO insertedPO = configMapper.selectById(newPO.getId());
            return convertToVO(insertedPO);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String module, String key) {
        if (module == null || module.isEmpty() || key == null || key.isEmpty()) {
            throw new BusinessException("400", "模块名和配置键不能为空");
        }
        ConfigItemPO configPO = configMapper.selectByModuleAndKey(module, key);
        if (configPO == null) {
            throw new BusinessException("404", "配置项不存在: " + module + "." + key);
        }
        return configPO.getConfigValue();
    }

    /**
     * 转换为 VO
     */
    private ConfigItemVO convertToVO(ConfigItemPO po) {
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
}

