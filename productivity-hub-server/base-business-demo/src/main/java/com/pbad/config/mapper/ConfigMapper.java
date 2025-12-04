package com.pbad.config.mapper;

import com.pbad.config.domain.po.ConfigItemPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配置项 Mapper 接口.
 *
 * @author: system
 * @date: 2025-11-29
 * @version: 1.0
 */
public interface ConfigMapper {

    /**
     * 查询所有配置项
     *
     * @return 配置项列表
     */
    List<ConfigItemPO> selectAll();

    /**
     * 根据ID查询配置项
     *
     * @param id 配置ID
     * @return 配置项
     */
    ConfigItemPO selectById(@Param("id") String id);

    /**
     * 根据模块与配置键查询配置项
     *
     * @param module    模块名（如：sendgrid、dingtalk 等）
     * @param configKey 配置键名
     * @return 配置项
     */
    ConfigItemPO selectByModuleAndKey(@Param("module") String module, @Param("configKey") String configKey);

    /**
     * 更新配置项
     *
     * @param configItem 配置项
     * @return 更新行数
     */
    int updateConfig(ConfigItemPO configItem);
}

