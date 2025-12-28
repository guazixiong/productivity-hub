package com.pbad.image.mapper;

import com.pbad.image.domain.po.ImagePO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片信息Mapper接口.
 * 关联需求：REQ-IMG-001~REQ-IMG-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageMapper {

    /**
     * 插入图片记录
     */
    int insert(ImagePO image);

    /**
     * 根据ID和用户ID查询图片
     */
    ImagePO selectByIdAndUserId(@Param("id") String id, @Param("userId") String userId);

    /**
     * 根据ID查询图片（不校验用户ID，用于分享链接访问）
     */
    ImagePO selectById(@Param("id") String id);

    /**
     * 根据分享令牌查询图片
     */
    ImagePO selectByShareToken(@Param("shareToken") String shareToken);

    /**
     * 分页查询图片列表
     */
    List<ImagePO> selectByCondition(@Param("userId") String userId,
                                    @Param("category") String category,
                                    @Param("businessModule") String businessModule,
                                    @Param("businessId") String businessId,
                                    @Param("keyword") String keyword,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("status") String status,
                                    @Param("sortBy") String sortBy,
                                    @Param("sortOrder") String sortOrder);

    /**
     * 统计符合条件的图片数量
     */
    Long countByCondition(@Param("userId") String userId,
                         @Param("category") String category,
                         @Param("businessModule") String businessModule,
                         @Param("businessId") String businessId,
                         @Param("keyword") String keyword,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime,
                         @Param("status") String status);

    /**
     * 更新图片信息
     */
    int update(ImagePO image);

    /**
     * 批量更新图片状态（软删除）
     */
    int batchUpdateStatus(@Param("ids") List<String> ids,
                         @Param("userId") String userId,
                         @Param("status") String status,
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新图片状态
     */
    int updateStatus(@Param("id") String id,
                    @Param("userId") String userId,
                    @Param("status") String status,
                    @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新分享信息
     */
    int updateShareInfo(@Param("id") String id,
                       @Param("userId") String userId,
                       @Param("shareToken") String shareToken,
                       @Param("shareExpiresAt") LocalDateTime shareExpiresAt,
                       @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 清除分享信息
     */
    int clearShareInfo(@Param("id") String id,
                      @Param("userId") String userId,
                      @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 增加访问次数
     */
    int incrementAccessCount(@Param("id") String id);

    /**
     * 统计图片总数
     */
    Long countTotal(@Param("userId") String userId,
                   @Param("startTime") LocalDateTime startTime,
                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计总存储空间
     */
    Long sumFileSize(@Param("userId") String userId,
                    @Param("startTime") LocalDateTime startTime,
                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计最大文件大小
     */
    Long maxFileSize(@Param("userId") String userId,
                    @Param("startTime") LocalDateTime startTime,
                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计最小文件大小
     */
    Long minFileSize(@Param("userId") String userId,
                    @Param("startTime") LocalDateTime startTime,
                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按分类统计
     */
    List<ImagePO> groupByCategory(@Param("userId") String userId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 统计访问次数
     */
    Long sumAccessCount(@Param("userId") String userId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询热门图片（访问次数最多的前N张）
     */
    List<ImagePO> selectHotImages(@Param("userId") String userId,
                                 @Param("limit") int limit);

    /**
     * 按日期统计上传数量
     */
    List<ImagePO> groupByUploadDate(@Param("userId") String userId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 按日期统计访问数量
     */
    List<ImagePO> groupByAccessDate(@Param("userId") String userId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
}

