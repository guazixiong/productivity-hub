package com.pbad.image.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 图片压缩服务接口.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageCompressService {

    /**
     * 压缩图片到指定大小
     *
     * @param image       原始图片
     * @param formatName  图片格式
     * @param maxSize     最大文件大小（字节）
     * @return 压缩后的图片字节数组
     * @throws IOException 压缩失败时抛出
     */
    byte[] compressImage(BufferedImage image, String formatName, long maxSize) throws IOException;
}

