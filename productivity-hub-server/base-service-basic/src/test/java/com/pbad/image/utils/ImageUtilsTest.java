package com.pbad.image.utils;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图片工具类单元测试.
 * 关联测试用例：TC-IMG-UTILS-001至TC-IMG-UTILS-010
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
class ImageUtilsTest {

    /**
     * TC-IMG-UTILS-001: 从输入流读取图片-成功
     */
    @Test
    void testReadImage_FromInputStream_Success() throws IOException {
        BufferedImage testImage = createTestImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);

        BufferedImage result = ImageUtils.readImage(bais);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    /**
     * TC-IMG-UTILS-002: 从字节数组读取图片-成功
     */
    @Test
    void testReadImage_FromBytes_Success() throws IOException {
        BufferedImage testImage = createTestImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        BufferedImage result = ImageUtils.readImage(imageBytes);

        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    /**
     * TC-IMG-UTILS-003: 从输入流读取图片-无效数据
     */
    @Test
    void testReadImage_FromInputStream_InvalidData() {
        byte[] invalidBytes = new byte[]{1, 2, 3, 4, 5};
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidBytes);

        assertThrows(IOException.class, () -> {
            ImageUtils.readImage(bais);
        });
    }

    /**
     * TC-IMG-UTILS-004: 将BufferedImage写入字节数组-成功
     */
    @Test
    void testWriteImage_Success() throws IOException {
        BufferedImage testImage = createTestImage();

        byte[] result = ImageUtils.writeImage(testImage, "png");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * TC-IMG-UTILS-005: 获取图片尺寸-成功
     */
    @Test
    void testGetImageSize_Success() {
        BufferedImage testImage = createTestImage();

        int[] size = ImageUtils.getImageSize(testImage);

        assertNotNull(size);
        assertEquals(2, size.length);
        assertEquals(100, size[0]); // 宽度
        assertEquals(100, size[1]); // 高度
    }

    /**
     * TC-IMG-UTILS-006: 获取图片尺寸-空图片
     */
    @Test
    void testGetImageSize_NullImage() {
        int[] size = ImageUtils.getImageSize(null);

        assertNotNull(size);
        assertEquals(2, size.length);
        assertEquals(0, size[0]);
        assertEquals(0, size[1]);
    }

    /**
     * TC-IMG-UTILS-007: 验证图片是否有效-有效图片
     */
    @Test
    void testIsValidImage_Valid() {
        BufferedImage testImage = createTestImage();

        boolean result = ImageUtils.isValidImage(testImage);

        assertTrue(result);
    }

    /**
     * TC-IMG-UTILS-008: 验证图片是否有效-无效图片
     */
    @Test
    void testIsValidImage_Invalid() {
        assertFalse(ImageUtils.isValidImage(null));
    }

    /**
     * TC-IMG-UTILS-009: 获取文件扩展名对应的图片格式名
     */
    @Test
    void testGetFormatName() {
        assertEquals("jpeg", ImageUtils.getFormatName("jpg"));
        assertEquals("jpeg", ImageUtils.getFormatName("jpeg"));
        assertEquals("png", ImageUtils.getFormatName("png"));
        assertEquals("gif", ImageUtils.getFormatName("gif"));
        assertEquals("jpeg", ImageUtils.getFormatName(null));
    }

    /**
     * TC-IMG-UTILS-010: 根据MIME类型获取文件扩展名
     */
    @Test
    void testGetExtensionFromContentType() {
        assertEquals("jpg", ImageUtils.getExtensionFromContentType("image/jpeg"));
        assertEquals("png", ImageUtils.getExtensionFromContentType("image/png"));
        assertEquals("gif", ImageUtils.getExtensionFromContentType("image/gif"));
        assertEquals("webp", ImageUtils.getExtensionFromContentType("image/webp"));
        assertEquals("bmp", ImageUtils.getExtensionFromContentType("image/bmp"));
        assertEquals("jpg", ImageUtils.getExtensionFromContentType(null));
        assertEquals("jpg", ImageUtils.getExtensionFromContentType("unknown"));
    }

    /**
     * 创建测试用的BufferedImage对象
     */
    private BufferedImage createTestImage() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        return image;
    }
}

