package com.pbad.image.service;

import com.pbad.image.domain.vo.ImageVO;

/**
 * 图片访问服务接口.
 * 关联需求：REQ-IMG-001
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageAccessService {

    /**
     * 通过ID访问图片（增加访问统计）
     *
     * @param id     图片ID
     * @param userId 用户ID（可为空，用于分享链接访问）
     * @return 图片信息VO
     */
    ImageVO accessImageById(String id, String userId);

    /**
     * 通过分享令牌访问图片（增加访问统计）
     *
     * @param shareToken 分享令牌
     * @return 图片信息VO
     */
    ImageVO accessImageByShareToken(String shareToken);

    /**
     * 读取图片文件内容
     *
     * @param path 文件路径（相对路径）
     * @return 文件字节数组
     */
    byte[] readImageFile(String path);
}

