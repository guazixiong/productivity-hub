package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthWaterTargetPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 饮水目标配置Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthWaterTargetMapper {

    /**
     * 根据用户ID查询饮水目标配置
     *
     * @param userId 用户ID
     * @return 饮水目标配置
     */
    HealthWaterTargetPO selectByUserId(@Param("userId") String userId);

    /**
     * 插入饮水目标配置
     *
     * @param target 饮水目标配置
     * @return 影响行数
     */
    int insert(HealthWaterTargetPO target);

    /**
     * 更新饮水目标配置（需验证user_id）
     *
     * @param target 饮水目标配置
     * @return 影响行数
     */
    int update(HealthWaterTargetPO target);

    /**
     * 根据用户ID删除饮水目标配置
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 查询所有启用提醒的用户ID列表（已废弃，是否提醒由定时任务开关控制）
     *
     * @return 用户ID列表
     * @deprecated 是否提醒由定时任务开关控制，不再使用此方法
     */
    @Deprecated
    List<String> selectUserIdsWithReminderEnabled();

    /**
     * 查询所有有饮水目标配置的用户ID列表
     *
     * @return 用户ID列表
     */
    List<String> selectAllUserIds();
}

