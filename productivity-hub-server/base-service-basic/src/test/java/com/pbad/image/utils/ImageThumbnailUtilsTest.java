package com.pbad.image.utils;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缩略图生成工具类单元测试.
 * 关联测试用例：TC-IMG-THUMBNAIL-001至TC-IMG-THUMBNAIL-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
class ImageThumbnailUtilsTest {

    /**
     * TC-IMG-THUMBNAIL-001: 生成缩略图-成功
     */
    @Test
    void testGenerateThumbnail_Success() throws IOException {
        BufferedImage originalImage = createTestImage(800, 600);
        int maxWidth = 200;
        int maxHeight = 200;

        BufferedImage thumbnail = ImageThumbnailUtils.generateThumbnail(originalImage, maxWidth, maxHeight, 0.7f);

        assertNotNull(thumbnail);
        assertTrue(thumbnail.getWidth() <= maxWidth);
        assertTrue(thumbnail.getHeight() <= maxHeight);
        // 保持宽高比
        double originalRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        double thumbnailRatio = (double) thumbnail.getWidth() / thumbnail.getHeight();
        assertEquals(originalRatio, thumbnailRatio, 0.01);
    }

    /**
     * TC-IMG-THUMBNAIL-002: 生成缩略图-原图已小于缩略图尺寸
     */
    @Test
    void testGenerateThumbnail_OriginalSmaller() throws IOException {
        BufferedImage originalImage = createTestImage(100, 100);
        int maxWidth = 200;
        int maxHeight = 200;

        BufferedImage thumbnail = ImageThumbnailUtils.generateThumbnail(originalImage, maxWidth, maxHeight, 0.7f);

        assertNotNull(thumbnail);
        // 原图已经小于缩略图尺寸，应该返回原图
        assertEquals(originalImage.getWidth(), thumbnail.getWidth());
        assertEquals(originalImage.getHeight(), thumbnail.getHeight());
    }

    /**
     * TC-IMG-THUMBNAIL-003: 生成缩略图-图片为空
     */
    @Test
    void testGenerateThumbnail_NullImage() {
        assertThrows(IllegalArgumentException.class, () -> {
            ImageThumbnailUtils.generateThumbnail(null, 200, 200, 0.7f);
        });
    }

    /**
     * TC-IMG-THUMBNAIL-004: 生成缩略图-使用默认质量
     */
    @Test
    void testGenerateThumbnail_DefaultQuality() throws IOException {
        BufferedImage originalImage = createTestImage(800, 600);
        int maxWidth = 200;
        int maxHeight = 200;

        BufferedImage thumbnail = ImageThumbnailUtils.generateThumbnail(originalImage, maxWidth, maxHeight);

        assertNotNull(thumbnail);
        assertTrue(thumbnail.getWidth() <= maxWidth);
        assertTrue(thumbnail.getHeight() <= maxHeight);
    }

    /**
     * TC-IMG-THUMBNAIL-005: 生成缩略图-横向图片
     */
    @Test
    void testGenerateThumbnail_Landscape() throws IOException {
        BufferedImage originalImage = createTestImage(1200, 800);
        int maxWidth = 200;
        int maxHeight = 200;

        BufferedImage thumbnail = ImageThumbnailUtils.generateThumbnail(originalImage, maxWidth, maxHeight, 0.7f);

        assertNotNull(thumbnail);
        assertTrue(thumbnail.getWidth() <= maxWidth);
        assertTrue(thumbnail.getHeight() <= maxHeight);
        // 横向图片，宽度应该是限制因素
        assertEquals(maxWidth, thumbnail.getWidth());
    }

    /**
     * TC-IMG-THUMBNAIL-006: 生成缩略图-纵向图片
     */
    @Test
    void testGenerateThumbnail_Portrait() throws IOException {
        BufferedImage originalImage = createTestImage(800, 1200);
        int maxWidth = 200;
        int maxHeight = 200;

        BufferedImage thumbnail = ImageThumbnailUtils.generateThumbnail(originalImage, maxWidth, maxHeight, 0.7f);

        assertNotNull(thumbnail);
        assertTrue(thumbnail.getWidth() <= maxWidth);
        assertTrue(thumbnail.getHeight() <= maxHeight);
        // 纵向图片，高度应该是限制因素
        assertEquals(maxHeight, thumbnail.getHeight());
    }

    /**
     * 创建测试用的BufferedImage对象
     */
    private BufferedImage createTestImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return image;
    }
}

