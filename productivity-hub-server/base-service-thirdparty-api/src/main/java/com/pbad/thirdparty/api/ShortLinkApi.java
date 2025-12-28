package com.pbad.thirdparty.api;

/**
 * 短链服务API接口.
 * <p>提供URL短链生成和查询功能.</p>
 *
 * @author pbad
 */
public interface ShortLinkApi {

    /**
     * 创建短链.
     *
     * @param originalUrl 原始URL
     * @return 短链URL；失败时返回原始URL；不返回null
     */
    String createShortLink(String originalUrl);

    /**
     * 批量创建短链（并发处理）.
     *
     * @param originalUrls 原始URL列表
     * @return 短链URL列表（与输入顺序一致，每个元素都非null，失败时对应为原始URL）
     */
    java.util.List<String> batchCreateShortLink(java.util.List<String> originalUrls);
}

