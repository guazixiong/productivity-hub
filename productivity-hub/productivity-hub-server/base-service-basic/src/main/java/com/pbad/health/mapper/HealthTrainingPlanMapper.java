package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthTrainingPlanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 训练计划Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthTrainingPlanMapper {

    /**
     * 根据ID查询训练计划（需验证user_id）
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 训练计划
     */
    HealthTrainingPlanPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 查询训练计划列表
     *
     * @param userId   用户ID
     * @param status    状态筛选（可选）
     * @param planType  计划类型筛选（可选）
     * @return 训练计划列表
     */
    List<HealthTrainingPlanPO> selectList(
            @Param("userId") String userId,
            @Param("status") String status,
            @Param("planType") String planType
    );

    /**
     * 统计关联的运动记录数
     *
     * @param planId 计划ID
     * @param userId 用户ID
     * @return 关联记录数
     */
    long countExerciseRecords(@Param("planId") String planId, @Param("userId") String userId);

    /**
     * 插入训练计划
     *
     * @param plan 训练计划
     * @return 影响行数
     */
    int insert(HealthTrainingPlanPO plan);

    /**
     * 更新训练计划（需验证user_id）
     *
     * @param plan 训练计划
     * @return 影响行数
     */
    int update(HealthTrainingPlanPO plan);

    /**
     * 删除训练计划（需验证user_id）
     *
     * @param id     计划ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id, @Param("userId") String userId);
}

