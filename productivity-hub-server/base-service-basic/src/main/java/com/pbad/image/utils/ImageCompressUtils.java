package com.pbad.image.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 图片压缩工具类.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
public class ImageCompressUtils {

    /**
     * 压缩图片到指定大小（字节）
     *
     * @param image       原始图片
     * @param formatName  图片格式（如：jpeg、png）
     * @param maxSize     最大文件大小（字节）
     * @param quality     压缩质量（0.0-1.0）
     * @return 压缩后的图片字节数组
     * @throws IOException 压缩失败时抛出
     */
    public static byte[] compressImage(BufferedImage image, String formatName, long maxSize, float quality) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("图片不能为空");
        }

        // 先尝试不压缩，如果已经小于限制，直接返回
        byte[] originalBytes = ImageUtils.writeImage(image, formatName);
        if (originalBytes.length <= maxSize) {
            return originalBytes;
        }

        // 计算压缩比例
        float ratio = (float) maxSize / originalBytes.length;
        ratio = Math.min(ratio, 0.95f); // 最多压缩到95%，避免过度压缩

        // 计算新尺寸
        int newWidth = (int) (image.getWidth() * Math.sqrt(ratio));
        int newHeight = (int) (image.getHeight() * Math.sqrt(ratio));

        // 确保最小尺寸
        newWidth = Math.max(newWidth, 100);
        newHeight = Math.max(newHeight, 100);

        // 创建缩放后的图片
        BufferedImage scaledImage = scaleImage(image, newWidth, newHeight);

        // 尝试不同的质量值进行压缩
        float[] qualityLevels = {quality, quality * 0.9f, quality * 0.8f, quality * 0.7f, quality * 0.6f};
        for (float q : qualityLevels) {
            byte[] compressed = compressWithQuality(scaledImage, formatName, q);
            if (compressed.length <= maxSize) {
                return compressed;
            }
        }

        // 如果还是太大，继续缩小尺寸
        int attempts = 0;
        while (attempts < 5) {
            newWidth = (int) (newWidth * 0.9);
            newHeight = (int) (newHeight * 0.9);
            scaledImage = scaleImage(image, newWidth, newHeight);
            byte[] compressed = compressWithQuality(scaledImage, formatName, quality * 0.6f);
            if (compressed.length <= maxSize) {
                return compressed;
            }
            attempts++;
        }

        // 最后一次尝试，使用最低质量
        return compressWithQuality(scaledImage, formatName, 0.5f);
    }

    /**
     * 缩放图片
     *
     * @param image    原始图片
     * @param newWidth 新宽度
     * @param newHeight 新高度
     * @return 缩放后的图片
     */
    private static BufferedImage scaleImage(BufferedImage image, int newWidth, int newHeight) {
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaledImage;
    }

    /**
     * 使用指定质量压缩图片
     *
     * @param image     图片
     * @param formatName 格式名
     * @param quality   质量（0.0-1.0）
     * @return 压缩后的字节数组
     * @throws IOException 压缩失败时抛出
     */
    private static byte[] compressWithQuality(BufferedImage image, String formatName, float quality) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // JPEG格式支持质量压缩
            if ("jpeg".equals(formatName) || "jpg".equals(formatName)) {
                javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
                writer.setOutput(ios);
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality);
                }
                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
                writer.dispose();
                ios.close();
            } else {
                // 其他格式直接写入
                ImageIO.write(image, formatName, outputStream);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("压缩图片失败", e);
            throw new IOException("图片压缩失败", e);
        }
    }
}

