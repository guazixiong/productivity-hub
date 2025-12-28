package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthUserBodyInfoPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户身体信息Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthUserBodyInfoMapper {

    /**
     * 根据用户ID查询用户身体信息
     *
     * @param userId 用户ID
     * @return 用户身体信息
     */
    HealthUserBodyInfoPO selectByUserId(@Param("userId") String userId);

    /**
     * 插入用户身体信息
     *
     * @param bodyInfo 用户身体信息
     * @return 影响行数
     */
    int insert(HealthUserBodyInfoPO bodyInfo);

    /**
     * 更新用户身体信息（需验证user_id）
     *
     * @param bodyInfo 用户身体信息
     * @return 影响行数
     */
    int update(HealthUserBodyInfoPO bodyInfo);

    /**
     * 根据用户ID删除用户身体信息
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 查询所有用户的身体信息
     * 注意：邮箱地址从用户全局配置中读取，不在本表查询
     * 是否推送由定时任务控制，与用户身体信息无关
     *
     * @return 用户身体信息列表
     */
    List<HealthUserBodyInfoPO> selectPDUsers();

    /**
     * 查询所有用户身体信息（用于诊断）
     *
     * @return 用户身体信息列表
     */
    List<HealthUserBodyInfoPO> selectAll();
}

