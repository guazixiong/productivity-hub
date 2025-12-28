package com.pbad.image.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件存储工具类单元测试.
 * 关联测试用例：TC-IMG-STORAGE-001至TC-IMG-STORAGE-010
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
class FileStorageUtilsTest {

    @TempDir
    Path tempDir;

    private String baseDir;

    @BeforeEach
    void setUp() {
        baseDir = tempDir.toAbsolutePath().toString();
    }

    @AfterEach
    void tearDown() {
        // TempDir会自动清理
    }

    /**
     * TC-IMG-STORAGE-001: 生成存储路径-成功
     */
    @Test
    void testGenerateStoragePath_Success() {
        String path = FileStorageUtils.generateStoragePath("avatar", "jpg");

        assertNotNull(path);
        assertTrue(path.contains("avatar"));
        assertTrue(path.endsWith(".jpg"));
        assertTrue(path.contains(File.separator));
    }

    /**
     * TC-IMG-STORAGE-002: 生成缩略图存储路径-成功
     */
    @Test
    void testGenerateThumbnailPath_Success() {
        String path = FileStorageUtils.generateThumbnailPath("avatar", "jpg");

        assertNotNull(path);
        assertTrue(path.contains("avatar"));
        assertTrue(path.contains("thumbnails"));
        assertTrue(path.endsWith(".jpg"));
    }

    /**
     * TC-IMG-STORAGE-003: 确保目录存在-成功
     */
    @Test
    void testEnsureDirectoryExists_Success() throws IOException {
        String path = "test/subdir/file.jpg";

        assertDoesNotThrow(() -> {
            FileStorageUtils.ensureDirectoryExists(baseDir, path);
        });

        File file = new File(baseDir, path);
        assertTrue(file.getParentFile().exists());
    }

    /**
     * TC-IMG-STORAGE-004: 保存文件-成功
     */
    @Test
    void testSaveFile_Success() throws IOException {
        String path = "test/file.jpg";
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};

        String result = FileStorageUtils.saveFile(baseDir, path, fileBytes);

        assertNotNull(result);
        File file = new File(baseDir, path);
        assertTrue(file.exists());
        assertArrayEquals(fileBytes, Files.readAllBytes(file.toPath()));
    }

    /**
     * TC-IMG-STORAGE-005: 读取文件-成功
     */
    @Test
    void testReadFile_Success() throws IOException {
        String path = "test/file.jpg";
        byte[] originalBytes = new byte[]{1, 2, 3, 4, 5};
        FileStorageUtils.saveFile(baseDir, path, originalBytes);

        byte[] result = FileStorageUtils.readFile(baseDir, path);

        assertNotNull(result);
        assertArrayEquals(originalBytes, result);
    }

    /**
     * TC-IMG-STORAGE-006: 读取文件-文件不存在
     */
    @Test
    void testReadFile_NotFound() {
        String path = "test/nonexistent.jpg";

        assertThrows(IOException.class, () -> {
            FileStorageUtils.readFile(baseDir, path);
        });
    }

    /**
     * TC-IMG-STORAGE-007: 删除文件-成功
     */
    @Test
    void testDeleteFile_Success() throws IOException {
        String path = "test/file.jpg";
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        FileStorageUtils.saveFile(baseDir, path, fileBytes);

        boolean result = FileStorageUtils.deleteFile(baseDir, path);

        assertTrue(result);
        File file = new File(baseDir, path);
        assertFalse(file.exists());
    }

    /**
     * TC-IMG-STORAGE-008: 删除文件-文件不存在
     */
    @Test
    void testDeleteFile_NotFound() {
        String path = "test/nonexistent.jpg";

        boolean result = FileStorageUtils.deleteFile(baseDir, path);

        assertFalse(result);
    }

    /**
     * TC-IMG-STORAGE-009: 检查文件是否存在-存在
     */
    @Test
    void testFileExists_Exists() throws IOException {
        String path = "test/file.jpg";
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        FileStorageUtils.saveFile(baseDir, path, fileBytes);

        boolean result = FileStorageUtils.fileExists(baseDir, path);

        assertTrue(result);
    }

    /**
     * TC-IMG-STORAGE-010: 检查文件是否存在-不存在
     */
    @Test
    void testFileExists_NotExists() {
        String path = "test/nonexistent.jpg";

        boolean result = FileStorageUtils.fileExists(baseDir, path);

        assertFalse(result);
    }

    /**
     * TC-IMG-STORAGE-011: 获取文件大小-成功
     */
    @Test
    void testGetFileSize_Success() throws IOException {
        String path = "test/file.jpg";
        byte[] fileBytes = new byte[]{1, 2, 3, 4, 5};
        FileStorageUtils.saveFile(baseDir, path, fileBytes);

        long size = FileStorageUtils.getFileSize(baseDir, path);

        assertEquals(fileBytes.length, size);
    }

    /**
     * TC-IMG-STORAGE-012: 获取文件大小-文件不存在
     */
    @Test
    void testGetFileSize_NotFound() {
        String path = "test/nonexistent.jpg";

        long size = FileStorageUtils.getFileSize(baseDir, path);

        assertEquals(0, size);
    }

    /**
     * TC-IMG-STORAGE-013: 生成文件访问URL-成功
     */
    @Test
    void testGenerateFileUrl_Success() {
        String urlPrefix = "http://localhost:8080/uploads";
        String path = "avatar" + File.separator + "2025-01" + File.separator + "test.jpg";

        String url = FileStorageUtils.generateFileUrl(urlPrefix, path);

        assertNotNull(url);
        assertTrue(url.startsWith(urlPrefix));
        assertTrue(url.contains("test.jpg"));
        // 路径分隔符应该被转换为URL路径分隔符
        assertFalse(url.contains(File.separator));
    }

    /**
     * TC-IMG-STORAGE-014: 生成文件访问URL-路径为空
     */
    @Test
    void testGenerateFileUrl_EmptyPath() {
        String urlPrefix = "http://localhost:8080/uploads";

        String url = FileStorageUtils.generateFileUrl(urlPrefix, null);

        assertNull(url);
    }
}

