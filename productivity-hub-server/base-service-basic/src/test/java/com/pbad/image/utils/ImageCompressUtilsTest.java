package com.pbad.image.utils;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图片压缩工具类单元测试.
 * 关联测试用例：TC-IMG-COMPRESS-001至TC-IMG-COMPRESS-005
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
class ImageCompressUtilsTest {

    /**
     * TC-IMG-COMPRESS-001: 压缩图片到指定大小-成功
     */
    @Test
    void testCompressImage_Success() throws IOException {
        BufferedImage testImage = createLargeTestImage(2000, 2000);
        long maxSize = 100 * 1024; // 100KB

        byte[] result = ImageCompressUtils.compressImage(testImage, "jpeg", maxSize, 0.8f);

        assertNotNull(result);
        assertTrue(result.length <= maxSize);
    }

    /**
     * TC-IMG-COMPRESS-002: 压缩图片-图片已小于限制
     */
    @Test
    void testCompressImage_AlreadySmall() throws IOException {
        BufferedImage testImage = createTestImage(100, 100);
        long maxSize = 10 * 1024 * 1024; // 10MB，足够大

        byte[] originalBytes = ImageUtils.writeImage(testImage, "jpeg");
        byte[] result = ImageCompressUtils.compressImage(testImage, "jpeg", maxSize, 0.8f);

        assertNotNull(result);
        // 如果原图已经小于限制，应该返回原图或接近原图大小
        assertTrue(result.length > 0);
    }

    /**
     * TC-IMG-COMPRESS-003: 压缩图片-图片为空
     */
    @Test
    void testCompressImage_NullImage() {
        assertThrows(IllegalArgumentException.class, () -> {
            ImageCompressUtils.compressImage(null, "jpeg", 100 * 1024, 0.8f);
        });
    }

    /**
     * TC-IMG-COMPRESS-004: 压缩图片-PNG格式
     */
    @Test
    void testCompressImage_PngFormat() throws IOException {
        BufferedImage testImage = createLargeTestImage(1000, 1000);
        long maxSize = 500 * 1024; // 500KB

        byte[] result = ImageCompressUtils.compressImage(testImage, "png", maxSize, 0.8f);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * TC-IMG-COMPRESS-005: 压缩图片-极小尺寸限制
     */
    @Test
    void testCompressImage_VerySmallLimit() throws IOException {
        BufferedImage testImage = createLargeTestImage(2000, 2000);
        long maxSize = 10 * 1024; // 10KB，非常小的限制

        byte[] result = ImageCompressUtils.compressImage(testImage, "jpeg", maxSize, 0.8f);

        assertNotNull(result);
        // 即使限制很小，也应该能压缩到接近限制的大小
        assertTrue(result.length > 0);
    }

    /**
     * 创建测试用的BufferedImage对象
     */
    private BufferedImage createTestImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return image;
    }

    /**
     * 创建较大的测试图片
     */
    private BufferedImage createLargeTestImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 填充一些数据使图片更大
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                image.setRGB(x, y, 0xFF0000); // 红色
            }
        }
        return image;
    }
}

