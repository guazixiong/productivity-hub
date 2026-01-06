package com.pbad.health.mapper;

import com.pbad.health.domain.po.HealthWeightRecordPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 体重记录Mapper接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Mapper
public interface HealthWeightRecordMapper {

    /**
     * 根据ID查询体重记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 体重记录
     */
    HealthWeightRecordPO selectById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 分页查询体重记录列表
     *
     * @param userId    用户ID
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @param sortField 排序字段（可选）
     * @param sortOrder 排序方向（可选）
     * @param offset    偏移量
     * @param limit     限制条数
     * @return 体重记录列表
     */
    List<HealthWeightRecordPO> selectByPage(
            @Param("userId") String userId,
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
     * @param userId    用户ID
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 记录数
     */
    long countByCondition(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 插入体重记录
     *
     * @param record 体重记录
     * @return 影响行数
     */
    int insert(HealthWeightRecordPO record);

    /**
     * 更新体重记录（需验证user_id）
     *
     * @param record 体重记录
     * @return 影响行数
     */
    int update(HealthWeightRecordPO record);

    /**
     * 删除体重记录（需验证user_id）
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id, @Param("userId") String userId);

    /**
     * 批量删除体重记录（需验证user_id）
     *
     * @param ids    记录ID列表
     * @param userId 用户ID
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<String> ids, @Param("userId") String userId);

    /**
     * 查询趋势数据（按日期分组，返回每日最新体重）
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势数据列表（日期和体重）
     */
    List<java.util.Map<String, Object>> queryTrendData(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 查询指定日期范围内的最新体重记录
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 最新体重记录
     */
    HealthWeightRecordPO selectLatestByDateRange(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 查询指定日期范围内的最早体重记录
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 最早体重记录
     */
    HealthWeightRecordPO selectEarliestByDateRange(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /**
     * 统计体重数据（按日期范围）
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据（平均体重、最高体重、最低体重、记录数）
     */
    java.util.Map<String, Object> statisticsByDateRange(
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
     * @param userId    用户ID
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @param sortField 排序字段（可选）
     * @param sortOrder 排序方向（可选）
     * @return 体重记录列表
     */
    List<HealthWeightRecordPO> selectAll(
            @Param("userId") String userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 批量更新BMI（根据新的身高重新计算所有历史记录的BMI）
     *
     * @param userId   用户ID
     * @param heightCm 新的身高（厘米）
     * @return 影响行数
     */
    int batchUpdateBmiByHeight(@Param("userId") String userId, @Param("heightCm") java.math.BigDecimal heightCm);
}

