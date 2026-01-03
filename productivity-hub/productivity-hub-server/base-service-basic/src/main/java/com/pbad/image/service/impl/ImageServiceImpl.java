package com.pbad.image.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pbad.generator.api.IdGeneratorApi;
import com.pbad.image.config.ImageProperties;
import com.pbad.image.domain.dto.ImageQueryDTO;
import com.pbad.image.domain.dto.ImageUpdateDTO;
import com.pbad.image.domain.po.ImagePO;
import com.pbad.image.domain.vo.ImageStatisticsVO;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.mapper.ImageMapper;
import com.pbad.image.service.ImageCompressService;
import com.pbad.image.service.ImageService;
import com.pbad.image.service.ImageStorageService;
import com.pbad.image.service.ImageThumbnailService;
import com.pbad.image.constants.ImageErrorCode;
import com.pbad.image.utils.ImageUtils;
import common.core.domain.PageResult;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片核心业务服务实现类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DELETED = "DELETED";
    private static final String STATUS_ARCHIVED = "ARCHIVED";

    private final ImageMapper imageMapper;
    private final ImageStorageService imageStorageService;
    private final ImageCompressService imageCompressService;
    private final ImageThumbnailService imageThumbnailService;
    private final ImageProperties imageProperties;
    private final IdGeneratorApi idGeneratorApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO uploadImage(MultipartFile file, String category, String businessModule,
                              String businessId, String description, String userId) {
        // 验证文件
        validateFile(file);

        try {
            // 读取文件字节
            byte[] fileBytes = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();
            String extension = getFileExtension(originalFilename);

            // 验证文件类型和大小
            validateFileType(contentType, extension);
            validateFileSize(fileBytes.length);

            // 读取图片信息
            BufferedImage image = ImageUtils.readImage(fileBytes);
            int width = image.getWidth();
            int height = image.getHeight();

            // 压缩图片（如果需要）
            byte[] compressedBytes = fileBytes;
            if (fileBytes.length > imageProperties.getMaxFileSize()) {
                compressedBytes = imageCompressService.compressImage(image, getFormatName(extension), imageProperties.getMaxFileSize());
            }

            // 生成缩略图
            BufferedImage thumbnail = imageThumbnailService.generateThumbnail(image);
            byte[] thumbnailBytes = ImageUtils.writeImage(thumbnail, ImageUtils.getFormatName(extension));

            // 保存文件
            String filePath = imageStorageService.saveImage(category, extension, compressedBytes);
            String thumbnailPath = imageStorageService.saveThumbnail(category, extension, thumbnailBytes);

            // 生成访问URL
            String fileUrl = imageStorageService.generateFileUrl(filePath);
            String thumbnailUrl = imageStorageService.generateThumbnailUrl(thumbnailPath);

            // 生成存储文件名
            String storedFilename = extractFilenameFromPath(filePath);

            // 创建图片记录
            ImagePO imagePO = new ImagePO();
            imagePO.setId(idGeneratorApi.generateId());
            imagePO.setUserId(userId);
            imagePO.setOriginalFilename(originalFilename);
            imagePO.setStoredFilename(storedFilename);
            imagePO.setFilePath(filePath);
            imagePO.setFileUrl(fileUrl);
            imagePO.setFileSize((long) compressedBytes.length);
            imagePO.setFileType(contentType);
            imagePO.setFileExtension(extension);
            imagePO.setWidth(width);
            imagePO.setHeight(height);
            imagePO.setCategory(category != null ? category : "other");
            imagePO.setBusinessModule(businessModule);
            imagePO.setBusinessId(businessId);
            imagePO.setDescription(description);
            imagePO.setThumbnailPath(thumbnailPath);
            imagePO.setThumbnailUrl(thumbnailUrl);
            imagePO.setAccessCount(0L);
            imagePO.setStatus(STATUS_ACTIVE);
            imagePO.setCreatedAt(LocalDateTime.now());
            imagePO.setUpdatedAt(LocalDateTime.now());

            int inserted = imageMapper.insert(imagePO);
            if (inserted <= 0) {
                throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "保存图片记录失败");
            }

            return convertToVO(imagePO);
        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "上传图片失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ImageVO> batchUploadImages(MultipartFile[] files, String category,
                                          String businessModule, String businessId,
                                          String description, String userId) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "请选择要上传的图片文件");
        }

        if (files.length > imageProperties.getBatchMaxCount()) {
            throw new BusinessException(ImageErrorCode.BATCH_LIMIT_EXCEEDED, "批量上传数量不能超过" + imageProperties.getBatchMaxCount() + "张");
        }

        List<ImageVO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                ImageVO imageVO = uploadImage(file, category, businessModule, businessId, description, userId);
                results.add(imageVO);
            } catch (Exception e) {
                log.error("批量上传图片失败: {}", file.getOriginalFilename(), e);
                // 继续处理其他文件，不中断整个流程
            }
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ImageVO> listImages(ImageQueryDTO queryDTO, String userId) {
        // 参数校验
        if (queryDTO == null) {
            queryDTO = new ImageQueryDTO();
        }
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(20);
        }
        if (queryDTO.getPageSize() > 100) {
            queryDTO.setPageSize(100);
        }

        // 解析时间
        LocalDateTime startTime = parseDateTime(queryDTO.getStartTime());
        LocalDateTime endTime = parseDateTime(queryDTO.getEndTime());

        // 使用 PageHelper 进行分页
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 查询数据
        List<ImagePO> imageList = imageMapper.selectByCondition(
                userId,
                queryDTO.getCategory(),
                queryDTO.getBusinessModule(),
                queryDTO.getBusinessId(),
                queryDTO.getKeyword(),
                startTime,
                endTime,
                queryDTO.getStatus(),
                queryDTO.getSortBy(),
                queryDTO.getSortOrder()
        );

        // 获取分页信息
        PageInfo<ImagePO> pageInfo = new PageInfo<>(imageList);

        // 转换为 VO
        List<ImageVO> items = imageList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), items);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageVO getImageById(String id, String userId) {
        ImagePO imagePO = getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }
        return convertToVO(imagePO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO updateImage(String id, ImageUpdateDTO updateDTO, String userId) {
        ImagePO imagePO = getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        // 更新字段
        if (updateDTO != null) {
            if (StringUtils.hasText(updateDTO.getDescription())) {
                imagePO.setDescription(updateDTO.getDescription());
            }
            if (StringUtils.hasText(updateDTO.getBusinessModule())) {
                imagePO.setBusinessModule(updateDTO.getBusinessModule());
            }
            if (StringUtils.hasText(updateDTO.getBusinessId())) {
                imagePO.setBusinessId(updateDTO.getBusinessId());
            }
        }

        imagePO.setUpdatedAt(LocalDateTime.now());

        int updated = imageMapper.update(imagePO);
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "更新图片信息失败");
        }

        return convertToVO(imageMapper.selectByIdAndUserId(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(String id, String userId) {
        ImagePO imagePO = getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        if (STATUS_DELETED.equals(imagePO.getStatus())) {
            return; // 已经删除，直接返回
        }

        int updated = imageMapper.updateStatus(id, userId, STATUS_DELETED, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "删除图片失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteImages(List<String> ids, String userId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        int updated = imageMapper.batchUpdateStatus(ids, userId, STATUS_DELETED, LocalDateTime.now());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO restoreImage(String id, String userId) {
        ImagePO imagePO = getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        if (!STATUS_DELETED.equals(imagePO.getStatus())) {
            throw new BusinessException(ImageErrorCode.INVALID_STATUS_FOR_RESTORE, "只能恢复已删除的图片");
        }

        int updated = imageMapper.updateStatus(id, userId, STATUS_ACTIVE, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "恢复图片失败");
        }

        return convertToVO(imageMapper.selectByIdAndUserId(id, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO archiveImage(String id, String userId) {
        ImagePO imagePO = getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        if (STATUS_ARCHIVED.equals(imagePO.getStatus())) {
            return convertToVO(imagePO); // 已经归档，直接返回
        }

        int updated = imageMapper.updateStatus(id, userId, STATUS_ARCHIVED, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "归档图片失败");
        }

        return convertToVO(imageMapper.selectByIdAndUserId(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ImageStatisticsVO getStatistics(String userId, LocalDateTime startTime, LocalDateTime endTime) {
        ImageStatisticsVO statistics = new ImageStatisticsVO();

        // 统计总数
        Long totalCount = imageMapper.countTotal(userId, startTime, endTime);
        statistics.setTotalCount(totalCount != null ? totalCount : 0L);

        // 统计总存储空间
        Long totalSize = imageMapper.sumFileSize(userId, startTime, endTime);
        statistics.setTotalSize(totalSize != null ? totalSize : 0L);

        // 计算平均文件大小
        if (totalCount != null && totalCount > 0 && totalSize != null) {
            statistics.setAverageSize(totalSize / totalCount);
        } else {
            statistics.setAverageSize(0L);
        }

        // 统计最大和最小文件大小
        Long maxFileSize = imageMapper.maxFileSize(userId, startTime, endTime);
        Long minFileSize = imageMapper.minFileSize(userId, startTime, endTime);
        statistics.setMaxFileSize(maxFileSize != null ? maxFileSize : 0L);
        statistics.setMinFileSize(minFileSize != null ? minFileSize : 0L);

        // 按分类统计
        List<ImagePO> categoryStats = imageMapper.groupByCategory(userId, startTime, endTime);
        if (categoryStats != null) {
            List<ImageStatisticsVO.CategoryStat> categoryStatList = categoryStats.stream()
                    .map(po -> {
                        ImageStatisticsVO.CategoryStat stat = new ImageStatisticsVO.CategoryStat();
                        stat.setCategory(po.getCategory());
                        stat.setCount(po.getAccessCount()); // 这里假设accessCount存储的是数量
                        stat.setTotalSize(po.getFileSize()); // 这里假设fileSize存储的是总大小
                        return stat;
                    })
                    .collect(Collectors.toList());
            statistics.setCategoryStats(categoryStatList);
        }

        // 访问统计
        Long totalAccessCount = imageMapper.sumAccessCount(userId, startTime, endTime);
        ImageStatisticsVO.AccessStat accessStat = new ImageStatisticsVO.AccessStat();
        accessStat.setTotalAccessCount(totalAccessCount != null ? totalAccessCount : 0L);
        if (totalCount != null && totalCount > 0 && totalAccessCount != null) {
            accessStat.setAverageAccessCount(totalAccessCount / totalCount);
        } else {
            accessStat.setAverageAccessCount(0L);
        }
        // 最大访问次数需要从热门图片中获取
        List<ImagePO> hotImages = imageMapper.selectHotImages(userId, 1);
        if (hotImages != null && !hotImages.isEmpty()) {
            accessStat.setMaxAccessCount(hotImages.get(0).getAccessCount());
        } else {
            accessStat.setMaxAccessCount(0L);
        }
        statistics.setAccessStats(accessStat);

        // 热门图片
        List<ImagePO> hotImageList = imageMapper.selectHotImages(userId, 10);
        if (hotImageList != null) {
            List<ImageVO> hotImageVOList = hotImageList.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            statistics.setHotImages(hotImageVOList);
        }

        // 上传趋势
        List<ImagePO> uploadTrendList = imageMapper.groupByUploadDate(userId, startTime, endTime);
        if (uploadTrendList != null) {
            List<ImageStatisticsVO.TrendItem> uploadTrend = uploadTrendList.stream()
                    .map(po -> {
                        ImageStatisticsVO.TrendItem item = new ImageStatisticsVO.TrendItem();
                        item.setDate(po.getCreatedAt() != null ? po.getCreatedAt().format(DATE_FORMATTER) : "");
                        item.setCount(po.getAccessCount()); // 这里假设accessCount存储的是数量
                        return item;
                    })
                    .collect(Collectors.toList());
            statistics.setUploadTrend(uploadTrend);
        }

        // 访问趋势
        List<ImagePO> accessTrendList = imageMapper.groupByAccessDate(userId, startTime, endTime);
        if (accessTrendList != null) {
            List<ImageStatisticsVO.TrendItem> accessTrend = accessTrendList.stream()
                    .map(po -> {
                        ImageStatisticsVO.TrendItem item = new ImageStatisticsVO.TrendItem();
                        item.setDate(po.getUpdatedAt() != null ? po.getUpdatedAt().format(DATE_FORMATTER) : "");
                        item.setCount(po.getAccessCount());
                        return item;
                    })
                    .collect(Collectors.toList());
            statistics.setAccessTrend(accessTrend);
        }

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public ImagePO getImagePOByIdAndUserId(String id, String userId) {
        return imageMapper.selectByIdAndUserId(id, userId);
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "请选择要上传的图片文件");
        }
    }

    /**
     * 验证文件类型
     */
    private void validateFileType(String contentType, String extension) {
        if (!StringUtils.hasText(contentType) || !imageProperties.getAllowedTypes().contains(contentType.toLowerCase())) {
            throw new BusinessException(ImageErrorCode.UNSUPPORTED_FILE_TYPE, "不支持的文件类型: " + contentType);
        }
        if (!StringUtils.hasText(extension) || !imageProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new BusinessException(ImageErrorCode.UNSUPPORTED_FILE_TYPE, "不支持的文件扩展名: " + extension);
        }
    }

    /**
     * 验证文件大小
     */
    private void validateFileSize(long fileSize) {
        if (fileSize > imageProperties.getMaxFileSize() * 10) { // 允许压缩后达到最大值的10倍
            throw new BusinessException(ImageErrorCode.FILE_SIZE_EXCEEDED, "文件大小超过限制");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "jpg";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "jpg";
    }

    /**
     * 获取格式名称
     */
    private String getFormatName(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            case "webp":
                return "webp";
            case "bmp":
                return "bmp";
            default:
                return "jpg";
        }
    }

    /**
     * 从路径中提取文件名
     */
    private String extractFilenameFromPath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        int lastSeparator = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSeparator >= 0 && lastSeparator < path.length() - 1) {
            return path.substring(lastSeparator + 1);
        }
        return path;
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("解析日期时间失败: {}", dateTimeStr, e);
            return null;
        }
    }

    /**
     * PO转VO
     */
    private ImageVO convertToVO(ImagePO po) {
        if (po == null) {
            return null;
        }
        ImageVO vo = new ImageVO();
        vo.setId(po.getId());
        vo.setOriginalFilename(po.getOriginalFilename());
        vo.setStoredFilename(po.getStoredFilename());
        vo.setFileUrl(po.getFileUrl());
        vo.setThumbnailUrl(po.getThumbnailUrl());
        vo.setFileSize(po.getFileSize());
        vo.setWidth(po.getWidth());
        vo.setHeight(po.getHeight());
        vo.setCategory(po.getCategory());
        vo.setBusinessModule(po.getBusinessModule());
        vo.setBusinessId(po.getBusinessId());
        vo.setDescription(po.getDescription());
        vo.setAccessCount(po.getAccessCount());
        vo.setStatus(po.getStatus());
        vo.setCreatedAt(po.getCreatedAt());
        vo.setUpdatedAt(po.getUpdatedAt());
        return vo;
    }
}

