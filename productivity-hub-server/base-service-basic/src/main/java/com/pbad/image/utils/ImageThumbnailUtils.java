package com.pbad.image.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 缩略图生成工具类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
public class ImageThumbnailUtils {

    /**
     * 生成缩略图
     *
     * @param image       原始图片
     * @param maxWidth    最大宽度
     * @param maxHeight   最大高度
     * @param quality     质量（0.0-1.0）
     * @return 缩略图
     * @throws IOException 生成失败时抛出
     */
    public static BufferedImage generateThumbnail(BufferedImage image, int maxWidth, int maxHeight, float quality) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("图片不能为空");
        }

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // 计算缩放比例，保持宽高比
        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        
        // 如果原图已经小于缩略图尺寸，直接返回原图
        if (scale >= 1.0) {
            return image;
        }

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // 确保最小尺寸为1
        newWidth = Math.max(newWidth, 1);
        newHeight = Math.max(newHeight, 1);

        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();

        // 设置高质量渲染
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        // 绘制缩放后的图片
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return thumbnail;
    }

    /**
     * 生成缩略图（使用默认质量0.7）
     *
     * @param image     原始图片
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 缩略图
     * @throws IOException 生成失败时抛出
     */
    public static BufferedImage generateThumbnail(BufferedImage image, int maxWidth, int maxHeight) throws IOException {
        return generateThumbnail(image, maxWidth, maxHeight, 0.7f);
    }
}

