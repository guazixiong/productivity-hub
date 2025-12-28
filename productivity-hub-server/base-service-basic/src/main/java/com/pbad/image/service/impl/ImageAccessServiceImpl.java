package com.pbad.image.service.impl;

import com.pbad.image.config.ImageProperties;
import com.pbad.image.domain.po.ImagePO;
import com.pbad.image.domain.vo.ImageVO;
import com.pbad.image.mapper.ImageMapper;
import com.pbad.image.constants.ImageErrorCode;
import com.pbad.image.service.ImageAccessService;
import com.pbad.image.service.ImageService;
import com.pbad.image.service.ImageStorageService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 图片访问服务实现类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAccessServiceImpl implements ImageAccessService {

    private final ImageMapper imageMapper;
    private final ImageService imageService;
    private final ImageStorageService imageStorageService;
    private final ImageProperties imageProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO accessImageById(String id, String userId) {
        ImagePO imagePO;
        if (StringUtils.hasText(userId)) {
            // 通过用户ID验证权限
            imagePO = imageService.getImagePOByIdAndUserId(id, userId);
            if (imagePO == null) {
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
            }
        } else {
            // 不验证用户ID（用于分享链接访问）
            imagePO = imageMapper.selectById(id);
            if (imagePO == null) {
                throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
            }
        }

        // 增加访问统计
        if (imageProperties.isEnableAccessStatistics()) {
            imageMapper.incrementAccessCount(id);
        }

        return imageService.getImageById(id, imagePO.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageVO accessImageByShareToken(String shareToken) {
        if (!StringUtils.hasText(shareToken)) {
            throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "分享令牌不能为空");
        }

        // 根据分享令牌查询图片
        ImagePO imagePO = imageMapper.selectByShareToken(shareToken);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.SHARE_NOT_FOUND_OR_EXPIRED, "分享链接不存在或已失效");
        }

        // 检查是否过期
        if (imagePO.getShareExpiresAt() != null && imagePO.getShareExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(ImageErrorCode.SHARE_NOT_FOUND_OR_EXPIRED, "分享链接已过期");
        }

        // 增加访问统计
        if (imageProperties.isEnableAccessStatistics()) {
            imageMapper.incrementAccessCount(imagePO.getId());
        }

        return imageService.getImageById(imagePO.getId(), imagePO.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] readImageFile(String path) {
        if (!StringUtils.hasText(path)) {
            throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "文件路径不能为空");
        }

        try {
            return imageStorageService.readImage(path);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", path, e);
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "读取图片文件失败: " + e.getMessage());
        }
    }
}

