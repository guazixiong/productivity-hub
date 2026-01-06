package com.pbad.config.service;

import com.pbad.config.domain.dto.ConfigCreateOrUpdateDTO;
import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.vo.ConfigItemVO;

import java.util.List;

/**
 * 配置服务接口.
 *
 * @author: pbad
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface ConfigService {

    /**
     * 获取配置列表
     *
     * @return 配置项列表
     */
    List<ConfigItemVO> getConfigList(String userId);

    /**
     * 更新配置项
     *
     * @param updateDTO 更新请求
     * @param updatedBy 更新人
     * @return 更新后的配置项
     */
    ConfigItemVO updateConfig(ConfigUpdateDTO updateDTO, String userId, String updatedBy);

    /**
     * 创建或更新配置项（通过 module 和 key）
     *
     * @param createOrUpdateDTO 创建或更新请求
     * @param updatedBy 更新人
     * @return 创建或更新后的配置项
     */
    ConfigItemVO createOrUpdateConfig(ConfigCreateOrUpdateDTO createOrUpdateDTO, String userId, String updatedBy);

    /**
     * 根据模块与配置键获取配置值
     *
     * @param module 模块名（如：sendgrid、dingtalk 等）
     * @param key    配置键名（如：sendgrid.apiKey）
     * @param userId 用户ID，普通用户读取自身全局配置，模板用户读取模板配置
     * @return 配置值
     */
    String getConfigValue(String module, String key, String userId);

    /**
     * 直接从模板全局配置读取配置值（不依赖用户副本）
     *
     * @param module 模块名
     * @param key    配置键名
     * @return 模板配置值
     */
    String getTemplateConfigValue(String module, String key);

    /**
     * 为指定用户根据全局模板初始化配置
     *
     * @param userId 用户ID
     * @param operator 操作人
     */
    void initializeUserConfigFromTemplate(String userId, String operator);
}

