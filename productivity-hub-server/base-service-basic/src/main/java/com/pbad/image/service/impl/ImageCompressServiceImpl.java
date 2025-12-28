package com.pbad.image.service.impl;

import com.pbad.image.config.ImageProperties;
import com.pbad.image.service.ImageCompressService;
import com.pbad.image.utils.ImageCompressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 图片压缩服务实现类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCompressServiceImpl implements ImageCompressService {

    private final ImageProperties imageProperties;

    @Override
    public byte[] compressImage(BufferedImage image, String formatName, long maxSize) throws IOException {
        return ImageCompressUtils.compressImage(image, formatName, maxSize, (float) imageProperties.getCompressQuality());
    }
}

