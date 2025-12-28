package com.pbad.image.service.impl;

import com.pbad.image.config.ImageProperties;
import com.pbad.image.service.ImageStorageService;
import com.pbad.image.utils.FileStorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 图片存储服务实现类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageServiceImpl implements ImageStorageService {

    private final ImageProperties imageProperties;

    @Override
    public String saveImage(String category, String extension, byte[] fileBytes) throws IOException {
        String path = FileStorageUtils.generateStoragePath(category, extension);
        FileStorageUtils.saveFile(imageProperties.getBaseDir(), path, fileBytes);
        return path;
    }

    @Override
    public String saveThumbnail(String category, String extension, byte[] fileBytes) throws IOException {
        String path = FileStorageUtils.generateThumbnailPath(category, extension);
        FileStorageUtils.saveFile(imageProperties.getBaseDir(), path, fileBytes);
        return path;
    }

    @Override
    public byte[] readImage(String path) throws IOException {
        return FileStorageUtils.readFile(imageProperties.getBaseDir(), path);
    }

    @Override
    public boolean deleteImage(String path) {
        return FileStorageUtils.deleteFile(imageProperties.getBaseDir(), path);
    }

    @Override
    public String generateFileUrl(String path) {
        return FileStorageUtils.generateFileUrl(imageProperties.getUrlPrefix(), path);
    }

    @Override
    public String generateThumbnailUrl(String path) {
        return FileStorageUtils.generateFileUrl(imageProperties.getUrlPrefix(), path);
    }
}

