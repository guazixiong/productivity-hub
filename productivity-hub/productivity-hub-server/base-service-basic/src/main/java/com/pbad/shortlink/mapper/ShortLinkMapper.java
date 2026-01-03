package com.pbad.shortlink.mapper;

import com.pbad.shortlink.domain.po.ShortLinkPO;
import org.apache.ibatis.annotations.Param;

/**
 * 短链 Mapper 接口.
 *
 * @author pbad
 */
public interface ShortLinkMapper {

    /**
     * 根据短链代码查询
     *
     * @param shortCode 短链代码
     * @return 短链信息
     */
    ShortLinkPO selectByShortCode(@Param("shortCode") String shortCode);

    /**
     * 根据原始URL查询
     *
     * @param originalUrl 原始URL
     * @return 短链信息
     */
    ShortLinkPO selectByOriginalUrl(@Param("originalUrl") String originalUrl);

    /**
     * 插入短链
     *
     * @param shortLink 短链信息
     * @return 插入行数
     */
    int insert(ShortLinkPO shortLink);

    /**
     * 更新访问次数
     *
     * @param shortCode 短链代码
     * @return 更新行数
     */
    int incrementAccessCount(@Param("shortCode") String shortCode);
}

