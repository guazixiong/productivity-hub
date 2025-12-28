package com.pbad.image.service;

import com.pbad.image.domain.dto.ImageShareDTO;
import com.pbad.image.domain.vo.ImageShareVO;

/**
 * 图片分享服务接口.
 * 关联需求：REQ-IMG-006
 *
 * @author: pbad
 * @date: 2025-01-XX
 * @version: 1.0
 */
public interface ImageShareService {

    /**
     * 创建分享链接
     *
     * @param id        图片ID
     * @param shareDTO  分享配置
     * @param userId    用户ID
     * @return 分享信息VO
     */
    ImageShareVO createShare(String id, ImageShareDTO shareDTO, String userId);

    /**
     * 取消分享
     *
     * @param id     图片ID
     * @param userId 用户ID
     */
    void cancelShare(String id, String userId);

    /**
     * 根据分享令牌获取图片信息
     *
     * @param shareToken 分享令牌
     * @return 图片信息VO
     */
    ImageShareVO getShareInfo(String shareToken);
}

