package com.pbad.config.service;

import com.pbad.config.domain.dto.ConfigUpdateDTO;
import com.pbad.config.domain.vo.ConfigItemVO;

import java.util.List;

/**
 * 配置服务接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface ConfigService {

    /**
     * 获取配置列表
     *
     * @return 配置项列表
     */
    List<ConfigItemVO> getConfigList();

    /**
     * 更新配置项
     *
     * @param updateDTO 更新请求
     * @param updatedBy 更新人
     * @return 更新后的配置项
     */
    ConfigItemVO updateConfig(ConfigUpdateDTO updateDTO, String updatedBy);

    /**
     * 根据模块与配置键获取配置值
     *
     * @param module 模块名（如：sendgrid、dingtalk 等）
     * @param key    配置键名（如：sendgrid.apiKey）
     * @return 配置值
     */
    String getConfigValue(String module, String key);
}

