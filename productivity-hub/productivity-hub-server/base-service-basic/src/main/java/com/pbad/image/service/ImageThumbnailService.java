package com.pbad.image.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 缩略图服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageThumbnailService {

    /**
     * 生成缩略图
     *
     * @param image 原始图片
     * @return 缩略图
     * @throws IOException 生成失败时抛出
     */
    BufferedImage generateThumbnail(BufferedImage image) throws IOException;
}

