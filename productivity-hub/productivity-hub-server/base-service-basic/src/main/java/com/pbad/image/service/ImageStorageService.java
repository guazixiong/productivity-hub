package com.pbad.image.service;

import java.io.IOException;

/**
 * 图片存储服务接口.
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageStorageService {

    /**
     * 保存图片文件
     *
     * @param category  图片分类
     * @param extension 文件扩展名
     * @param fileBytes 文件字节数组
     * @return 存储路径（相对路径）
     * @throws IOException 保存失败时抛出
     */
    String saveImage(String category, String extension, byte[] fileBytes) throws IOException;

    /**
     * 保存缩略图文件
     *
     * @param category  图片分类
     * @param extension 文件扩展名
     * @param fileBytes 文件字节数组
     * @return 存储路径（相对路径）
     * @throws IOException 保存失败时抛出
     */
    String saveThumbnail(String category, String extension, byte[] fileBytes) throws IOException;

    /**
     * 读取图片文件
     *
     * @param path 相对路径
     * @return 文件字节数组
     * @throws IOException 读取失败时抛出
     */
    byte[] readImage(String path) throws IOException;

    /**
     * 删除图片文件
     *
     * @param path 相对路径
     * @return true=删除成功，false=文件不存在或删除失败
     */
    boolean deleteImage(String path);

    /**
     * 生成文件访问URL
     *
     * @param path 相对路径
     * @return 访问URL
     */
    String generateFileUrl(String path);

    /**
     * 生成缩略图访问URL
     *
     * @param path 相对路径
     * @return 访问URL
     */
    String generateThumbnailUrl(String path);
}

