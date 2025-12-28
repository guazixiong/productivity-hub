package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthExerciseRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 运动记录Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthExerciseRecordMapper {

    /**
     * 根据ID查询运动记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 运动记录
     */
    HealthExerciseRecordPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 分页查询运动记录列表
     *
     * @param userId         用户ID
     * @param exerciseTypes  运动类型筛选（可选）
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @param trainingPlanId 训练计划ID（可选）
     * @param sortField      排序字段（可选）
     * @param sortOrder      排序方向（可选）
     * @param offset         偏移量
     * @param limit          限制条数
     * @return 运动记录列表
     */
    List<HealthExerciseRecordPO> selectByPage(
            @Param("userId") String userId,
            @Param("exerciseTypes") List<String> exerciseTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("trainingPlanId") String trainingPlanId,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计符合条件的记录数
     *
     * @param userId         用户ID
     * @param exerciseTypes  运动类型筛选（可选）
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @param trainingPlanId 训练计划ID（可选）
     * @return 记录数
     */
    long countByCondition(
            @Param("userId") String userId,
            @Param("exerciseTypes") List<String> exerciseTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("trainingPlanId") String trainingPlanId
    );

    /**
     * 插入运动记录
     *
     * @param record 运动记录
     * @return 影响行数
     */
    int insert(HealthExerciseRecordPO record);

    /**
     * 更新运动记录（需验证user_id）
     *
     * @param record 运动记录
     * @return 影响行数
     */
    int update(HealthExerciseRecordPO record);

    /**
     * 删除运动记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量删除运动记录（需验证user_id）
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<String> ids, @Param("userId") String userId);

    /**
     * 统计运动数据（按日期范围）
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据（总时长、总次数、总卡路里、总距离）
     */
    java.util.Map<String, Object> statisticsByDateRange(
            @Param("userId") String userId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );

    /**
     * 按运动类型分组统计
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 按类型统计列表
     */
    List<java.util.Map<String, Object>> statisticsByType(
            @Param("userId") String userId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );

    /**
     * 按训练计划分组统计
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 按计划统计列表
     */
    List<java.util.Map<String, Object>> statisticsByPlan(
            @Param("userId") String userId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );

    /**
     * 查询趋势数据（按日期分组）
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type      趋势类型（duration/count/calories）
     * @return 趋势数据列表（日期和数值）
     */
    List<java.util.Map<String, Object>> queryTrendData(
            @Param("userId") String userId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate,
            @Param("type") String type
    );

    /**
     * 查询有记录的日期列表
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期列表
     */
    List<java.util.Date> queryDatesWithRecords(
            @Param("userId") String userId,
            @Param("startDate") java.util.Date startDate,
            @Param("endDate") java.util.Date endDate
    );

    /**
     * 查询所有符合条件的记录（用于导出，不限制数量）
     *
     * @param userId         用户ID
     * @param exerciseTypes  运动类型筛选（可选）
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @param trainingPlanId 训练计划ID（可选）
     * @param sortField      排序字段（可选）
     * @param sortOrder      排序方向（可选）
     * @return 运动记录列表
     */
    List<HealthExerciseRecordPO> selectAll(
            @Param("userId") String userId,
            @Param("exerciseTypes") List<String> exerciseTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("trainingPlanId") String trainingPlanId,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );
}

