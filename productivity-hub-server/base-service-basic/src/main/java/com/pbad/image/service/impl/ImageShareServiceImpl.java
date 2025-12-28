package com.pbad.image.service.impl;

import com.pbad.image.domain.dto.ImageShareDTO;
import com.pbad.image.domain.po.ImagePO;
import com.pbad.image.constants.ImageErrorCode;
import com.pbad.image.domain.vo.ImageShareVO;
import com.pbad.image.mapper.ImageMapper;
import com.pbad.image.service.ImageService;
import com.pbad.image.service.ImageShareService;
import common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 图片分享服务实现类.
 * 关联需求：REQ-IMG-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageShareServiceImpl implements ImageShareService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ImageMapper imageMapper;
    private final ImageService imageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImageShareVO createShare(String id, ImageShareDTO shareDTO, String userId) {
        // 验证图片是否存在且属于当前用户
        ImagePO imagePO = imageService.getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        // 生成分享令牌
        String shareToken = UUID.randomUUID().toString().replace("-", "");

        // 解析过期时间
        LocalDateTime expiresAt = null;
        if (shareDTO != null && StringUtils.hasText(shareDTO.getExpiresAt())) {
            try {
                expiresAt = LocalDateTime.parse(shareDTO.getExpiresAt(), DATETIME_FORMATTER);
                if (expiresAt.isBefore(LocalDateTime.now())) {
                    throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "过期时间不能早于当前时间");
                }
            } catch (Exception e) {
                log.error("解析过期时间失败: {}", shareDTO.getExpiresAt(), e);
                throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "过期时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
            }
        }

        // 更新分享信息
        int updated = imageMapper.updateShareInfo(id, userId, shareToken, expiresAt, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "创建分享链接失败");
        }

        // 构建分享信息VO
        ImageShareVO shareVO = new ImageShareVO();
        shareVO.setId(id);
        shareVO.setShareToken(shareToken);
        shareVO.setShareUrl("/api/images/share/" + shareToken);
        shareVO.setExpiresAt(expiresAt);

        return shareVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelShare(String id, String userId) {
        // 验证图片是否存在且属于当前用户
        ImagePO imagePO = imageService.getImagePOByIdAndUserId(id, userId);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.IMAGE_NOT_FOUND, "图片不存在");
        }

        // 清除分享信息
        int updated = imageMapper.clearShareInfo(id, userId, LocalDateTime.now());
        if (updated <= 0) {
            throw new BusinessException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "取消分享失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImageShareVO getShareInfo(String shareToken) {
        if (!StringUtils.hasText(shareToken)) {
            throw new BusinessException(ImageErrorCode.INVALID_PARAMETER, "分享令牌不能为空");
        }

        // 根据分享令牌查询图片
        ImagePO imagePO = imageMapper.selectByShareToken(shareToken);
        if (imagePO == null) {
            throw new BusinessException(ImageErrorCode.SHARE_NOT_FOUND_OR_EXPIRED, "分享链接不存在或已失效");
        }

        // 检查是否过期
        if (imagePO.getShareExpiresAt() != null && imagePO.getShareExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ImageErrorCode.SHARE_NOT_FOUND_OR_EXPIRED, "分享链接已过期");
        }

        // 构建分享信息VO
        ImageShareVO shareVO = new ImageShareVO();
        shareVO.setId(imagePO.getId());
        shareVO.setShareToken(shareToken);
        shareVO.setShareUrl("/api/images/share/" + shareToken);
        shareVO.setExpiresAt(imagePO.getShareExpiresAt());

        return shareVO;
    }
}

