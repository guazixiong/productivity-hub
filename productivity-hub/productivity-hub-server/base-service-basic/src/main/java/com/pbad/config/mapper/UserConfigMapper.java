package com.pbad.config.mapper;

import com.pbad.config.domain.po.UserConfigPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户级配置 Mapper.
 */
public interface UserConfigMapper {

    /**
     * 根据用户查询配置列表
     */
    List<UserConfigPO> selectByUserId(@Param("userId") String userId);

    /**
     * 根据ID查询配置项
     */
    UserConfigPO selectById(@Param("id") String id);

    /**
     * 根据用户、模块、键查询配置项
     */
    UserConfigPO selectByUserAndKey(@Param("userId") String userId,
                                    @Param("module") String module,
                                    @Param("configKey") String configKey);

    /**
     * 统计用户配置数量
     */
    int countByUserId(@Param("userId") String userId);

    /**
     * 批量插入配置
     */
    int batchInsert(@Param("configs") List<UserConfigPO> configs);

    /**
     * 更新配置项
     */
    int updateConfig(UserConfigPO config);

    /**
     * 根据用户ID删除所有配置
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByUserId(@Param("userId") String userId);
}

