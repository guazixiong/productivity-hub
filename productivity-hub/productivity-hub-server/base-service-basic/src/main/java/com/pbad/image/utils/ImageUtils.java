package com.pbad.image.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片工具类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
public class ImageUtils {

    /**
     * 从输入流读取图片
     *
     * @param inputStream 输入流
     * @return BufferedImage对象
     * @throws IOException 读取失败时抛出
     */
    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            log.error("读取图片失败", e);
            throw new IOException("图片文件损坏或无法读取", e);
        }
    }

    /**
     * 从字节数组读取图片
     *
     * @param imageBytes 图片字节数组
     * @return BufferedImage对象
     * @throws IOException 读取失败时抛出
     */
    public static BufferedImage readImage(byte[] imageBytes) throws IOException {
        return readImage(new ByteArrayInputStream(imageBytes));
    }

    /**
     * 将BufferedImage写入字节数组
     *
     * @param image      BufferedImage对象
     * @param formatName 图片格式（如：jpg、png）
     * @return 字节数组
     * @throws IOException 写入失败时抛出
     */
    public static byte[] writeImage(BufferedImage image, String formatName) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("写入图片失败", e);
            throw new IOException("图片处理失败", e);
        }
    }

    /**
     * 获取图片尺寸
     *
     * @param image BufferedImage对象
     * @return int数组，[0]=宽度，[1]=高度
     */
    public static int[] getImageSize(BufferedImage image) {
        if (image == null) {
            return new int[]{0, 0};
        }
        return new int[]{image.getWidth(), image.getHeight()};
    }

    /**
     * 验证图片是否有效
     *
     * @param image BufferedImage对象
     * @return true=有效，false=无效
     */
    public static boolean isValidImage(BufferedImage image) {
        return image != null && image.getWidth() > 0 && image.getHeight() > 0;
    }

    /**
     * 获取文件扩展名对应的图片格式名（用于ImageIO）
     *
     * @param extension 文件扩展名（如：jpg、png）
     * @return 图片格式名（如：jpeg、png）
     */
    public static String getFormatName(String extension) {
        if (extension == null) {
            return "jpeg";
        }
        String ext = extension.toLowerCase();
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return "jpeg";
        }
        return ext;
    }

    /**
     * 根据MIME类型获取文件扩展名
     *
     * @param contentType MIME类型（如：image/jpeg）
     * @return 文件扩展名（如：jpg）
     */
    public static String getExtensionFromContentType(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        String type = contentType.toLowerCase();
        if (type.contains("jpeg")) {
            return "jpg";
        } else if (type.contains("png")) {
            return "png";
        } else if (type.contains("gif")) {
            return "gif";
        } else if (type.contains("webp")) {
            return "webp";
        } else if (type.contains("bmp")) {
            return "bmp";
        }
        return "jpg";
    }
}

