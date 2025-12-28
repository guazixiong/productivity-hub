package com.pbad.image.service.impl;

import com.pbad.image.config.ImageProperties;
import com.pbad.image.service.ImageThumbnailService;
import com.pbad.image.utils.ImageThumbnailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 缩略图服务实现类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageThumbnailServiceImpl implements ImageThumbnailService {

    private final ImageProperties imageProperties;

    @Override
    public BufferedImage generateThumbnail(BufferedImage image) throws IOException {
        return ImageThumbnailUtils.generateThumbnail(
                image,
                imageProperties.getThumbnailWidth(),
                imageProperties.getThumbnailHeight(),
                0.7f
        );
    }
}

