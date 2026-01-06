package com.pbad.image.service;

import com.pbad.image.domain.dto.ImageQueryDTO;
import com.pbad.image.domain.dto.ImageUpdateDTO;
import com.pbad.image.domain.po.ImagePO;
import com.pbad.image.domain.vo.ImageStatisticsVO;
import com.pbad.image.domain.vo.ImageVO;
import common.core.domain.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 图片核心业务服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageService {

    /**
     * 上传单张图片
     *
     * @param file           图片文件
     * @param category       图片分类
     * @param businessModule 业务模块标识
     * @param businessId     业务关联ID
     * @param description    图片描述
     * @param userId         用户ID
     * @return 图片信息VO
     */
    ImageVO uploadImage(MultipartFile file, String category, String businessModule,
                       String businessId, String description, String userId);

    /**
     * 批量上传图片
     *
     * @param files          图片文件数组
     * @param category       图片分类
     * @param businessModule 业务模块标识
     * @param businessId     业务关联ID
     * @param description    图片描述
     * @param userId         用户ID
     * @return 上传结果列表（包含成功和失败的记录）
     */
    List<ImageVO> batchUploadImages(MultipartFile[] files, String category,
                                   String businessModule, String businessId,
                                   String description, String userId);

    /**
     * 分页查询图片列表
     *
     * @param queryDTO 查询条件
     * @param userId   用户ID
     * @return 分页结果
     */
    PageResult<ImageVO> listImages(ImageQueryDTO queryDTO, String userId);

    /**
     * 查询图片详情
     *
     * @param id     图片ID
     * @param userId 用户ID
     * @return 图片信息VO
     */
    ImageVO getImageById(String id, String userId);

    /**
     * 更新图片信息
     *
     * @param id        图片ID
     * @param updateDTO 更新DTO
     * @param userId    用户ID
     * @return 更新后的图片信息VO
     */
    ImageVO updateImage(String id, ImageUpdateDTO updateDTO, String userId);

    /**
     * 删除图片（软删除）
     *
     * @param id     图片ID
     * @param userId 用户ID
     */
    void deleteImage(String id, String userId);

    /**
     * 批量删除图片（软删除）
     *
     * @param ids    图片ID列表
     * @param userId 用户ID
     * @return 成功删除的数量
     */
    int batchDeleteImages(List<String> ids, String userId);

    /**
     * 恢复图片
     *
     * @param id     图片ID
     * @param userId 用户ID
     * @return 恢复后的图片信息VO
     */
    ImageVO restoreImage(String id, String userId);

    /**
     * 归档图片
     *
     * @param id     图片ID
     * @param userId 用户ID
     * @return 归档后的图片信息VO
     */
    ImageVO archiveImage(String id, String userId);

    /**
     * 查询图片统计信息
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计信息VO
     */
    ImageStatisticsVO getStatistics(String userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID和用户ID查询图片PO（内部方法）
     *
     * @param id     图片ID
     * @param userId 用户ID
     * @return 图片PO
     */
    ImagePO getImagePOByIdAndUserId(String id, String userId);
}

