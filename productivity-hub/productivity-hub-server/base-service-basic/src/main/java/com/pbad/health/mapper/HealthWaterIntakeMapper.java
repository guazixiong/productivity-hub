package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthWaterIntakePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 饮水记录Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthWaterIntakeMapper {

    /**
     * 根据ID查询饮水记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 饮水记录
     */
    HealthWaterIntakePO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 分页查询饮水记录列表
     *
     * @param userId     用户ID
     * @param waterTypes 饮水类型筛选（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @param sortField  排序字段（可选）
     * @param sortOrder  排序方向（可选）
     * @param offset     偏移量
     * @param limit      限制条数
     * @return 饮水记录列表
     */
    List<HealthWaterIntakePO> selectByPage(
            @Param("userId") String userId,
            @Param("waterTypes") List<String> waterTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 统计符合条件的记录数
     *
     * @param userId     用户ID
     * @param waterTypes 饮水类型筛选（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @return 记录数
     */
    long countByCondition(
            @Param("userId") String userId,
            @Param("waterTypes") List<String> waterTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 插入饮水记录
     *
     * @param record 饮水记录
     * @return 影响行数
     */
    int insert(HealthWaterIntakePO record);

    /**
     * 更新饮水记录（需验证user_id）
     *
     * @param record 饮水记录
     * @return 影响行数
     */
    int update(HealthWaterIntakePO record);

    /**
     * 删除饮水记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量删除饮水记录（需验证user_id）
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<String> ids, @Param("userId") String userId);

    /**
     * 统计指定日期范围内的总饮水量
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 总饮水量（毫升）
     */
    Integer sumVolumeByDateRange(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 统计饮水数据（按日期范围）
     *
     * @param userId   用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据（总饮水量、总次数、达标天数）
     */
    java.util.Map<String, Object> statisticsByDateRange(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 统计指定日期的总饮水量
     *
     * @param userId    用户ID
     * @param intakeDate 饮水日期
     * @return 总饮水量（毫升）
     */
    Integer sumVolumeByDate(
            @Param("userId") String userId,
            @Param("intakeDate") Date intakeDate
    );

    /**
     * 按饮水类型分组统计
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 按类型统计列表
     */
    List<java.util.Map<String, Object>> statisticsByType(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 查询趋势数据（按日期分组）
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表（日期和总饮水量）
     */
    List<java.util.Map<String, Object>> queryTrendData(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 查询有记录的日期列表
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期列表
     */
    List<Date> queryDatesWithRecords(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 查询所有符合条件的记录（用于导出，不限制数量）
     *
     * @param userId     用户ID
     * @param waterTypes 饮水类型筛选（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @param sortField  排序字段（可选）
     * @param sortOrder  排序方向（可选）
     * @return 饮水记录列表
     */
    List<HealthWaterIntakePO> selectAll(
            @Param("userId") String userId,
            @Param("waterTypes") List<String> waterTypes,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );
}

