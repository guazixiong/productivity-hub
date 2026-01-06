package com.pbad.image.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储工具类.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
@Slf4j
public class FileStorageUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 生成存储路径（相对路径）
     * 格式：{category}/{yyyy-MM}/{uuid}.{extension}
     *
     * @param category  图片分类
     * @param extension 文件扩展名
     * @return 相对路径
     */
    public static String generateStoragePath(String category, String extension) {
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String filename = UUID.randomUUID().toString() + "." + extension;
        return category + File.separator + dateDir + File.separator + filename;
    }

    /**
     * 生成缩略图存储路径（相对路径）
     * 格式：{category}/{yyyy-MM}/thumbnails/{uuid}.{extension}
     *
     * @param category  图片分类
     * @param extension 文件扩展名
     * @return 相对路径
     */
    public static String generateThumbnailPath(String category, String extension) {
        String dateDir = LocalDate.now().format(DATE_FORMATTER);
        String filename = UUID.randomUUID().toString() + "." + extension;
        return category + File.separator + dateDir + File.separator + "thumbnails" + File.separator + filename;
    }

    /**
     * 确保目录存在，如果不存在则创建
     *
     * @param baseDir 基础目录
     * @param path    相对路径
     * @throws IOException 创建失败时抛出
     */
    public static void ensureDirectoryExists(String baseDir, String path) throws IOException {
        File baseDirFile = new File(baseDir);
        if (!baseDirFile.exists() && !baseDirFile.mkdirs()) {
            throw new IOException("创建基础目录失败: " + baseDir);
        }

        File file = new File(baseDir, path);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("创建目录失败: " + parentDir.getAbsolutePath());
            }
        }
    }

    /**
     * 保存文件
     *
     * @param baseDir    基础目录
     * @param path       相对路径
     * @param fileBytes  文件字节数组
     * @return 完整文件路径
     * @throws IOException 保存失败时抛出
     */
    public static String saveFile(String baseDir, String path, byte[] fileBytes) throws IOException {
        ensureDirectoryExists(baseDir, path);
        File file = new File(baseDir, path);
        Files.write(file.toPath(), fileBytes);
        return file.getAbsolutePath();
    }

    /**
     * 读取文件
     *
     * @param baseDir 基础目录
     * @param path    相对路径
     * @return 文件字节数组
     * @throws IOException 读取失败时抛出
     */
    public static byte[] readFile(String baseDir, String path) throws IOException {
        File file = new File(baseDir, path);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + file.getAbsolutePath());
        }
        return Files.readAllBytes(file.toPath());
    }

    /**
     * 删除文件
     *
     * @param baseDir 基础目录
     * @param path    相对路径
     * @return true=删除成功，false=文件不存在或删除失败
     */
    public static boolean deleteFile(String baseDir, String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        File file = new File(baseDir, path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 检查文件是否存在
     *
     * @param baseDir 基础目录
     * @param path    相对路径
     * @return true=存在，false=不存在
     */
    public static boolean fileExists(String baseDir, String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        File file = new File(baseDir, path);
        return file.exists() && file.isFile();
    }

    /**
     * 获取文件大小
     *
     * @param baseDir 基础目录
     * @param path    相对路径
     * @return 文件大小（字节），文件不存在返回0
     */
    public static long getFileSize(String baseDir, String path) {
        if (!StringUtils.hasText(path)) {
            return 0;
        }
        File file = new File(baseDir, path);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    /**
     * 生成文件访问URL
     *
     * @param urlPrefix URL前缀
     * @param path      相对路径
     * @return 访问URL
     */
    public static String generateFileUrl(String urlPrefix, String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        // 将Windows路径分隔符转换为URL路径分隔符
        String urlPath = path.replace(File.separator, "/");
        if (!urlPrefix.endsWith("/") && !urlPath.startsWith("/")) {
            return urlPrefix + "/" + urlPath;
        }
        return urlPrefix + urlPath;
    }
}

