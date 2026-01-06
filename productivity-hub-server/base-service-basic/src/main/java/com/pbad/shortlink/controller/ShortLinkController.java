package com.pbad.shortlink.controller;

import com.pbad.shortlink.domain.vo.ShortLinkCreateVO;
import com.pbad.shortlink.domain.vo.ShortLinkResponseVO;
import com.pbad.thirdparty.api.ShortLinkApi;
import common.core.domain.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 短链服务控制器.
 *
 * @author pbad
 */
@Slf4j
@RestController
@RequestMapping("/api/shortlink")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkApi shortLinkApi;

    /**
     * 创建短链
     *
     * @param request 创建请求
     * @return 短链信息
     */
    @PostMapping("/create")
    public ApiResponse<ShortLinkResponseVO> createShortLink(@RequestBody ShortLinkCreateVO request) {
        try {
            // 新逻辑：createShortLink 直接返回短链 URL；失败时返回原始 URL
            String shortLinkUrl = shortLinkApi.createShortLink(request.getOriginalUrl());

            ShortLinkResponseVO response = new ShortLinkResponseVO();
            // 兼容旧字段：shortCode 直接填充为短链 URL
            response.setShortCode(shortLinkUrl);
            response.setShortLinkUrl(shortLinkUrl);
            response.setOriginalUrl(request.getOriginalUrl());

            return ApiResponse.ok(response);
        } catch (Exception e) {
            log.error("创建短链失败", e);
            return ApiResponse.fail("创建短链失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建短链（并发处理）
     *
     * @param request 批量创建请求
     * @return 短链信息列表
     */
    @PostMapping("/batch-create")
    public ApiResponse<List<ShortLinkResponseVO>> batchCreateShortLink(@RequestBody List<ShortLinkCreateVO> request) {
        try {
            if (request == null || request.isEmpty()) {
                return ApiResponse.ok(new ArrayList<>());
            }

            // 提取URL列表，并记录与之对应的请求项，保证索引对齐
            List<String> originalUrls = new ArrayList<>();
            List<ShortLinkCreateVO> filteredRequests = new ArrayList<>();
            for (ShortLinkCreateVO item : request) {
                if (item != null && item.getOriginalUrl() != null) {
                    originalUrls.add(item.getOriginalUrl());
                    filteredRequests.add(item);
                }
            }

            // 批量创建短链（并发处理）
            List<String> shortLinks = shortLinkApi.batchCreateShortLink(originalUrls);

            // 构建响应列表
            List<ShortLinkResponseVO> responses = new ArrayList<>();
            for (int i = 0; i < filteredRequests.size() && i < shortLinks.size(); i++) {
                ShortLinkCreateVO item = filteredRequests.get(i);
                String shortLinkUrl = shortLinks.get(i);

                ShortLinkResponseVO response = new ShortLinkResponseVO();
                // 兼容旧字段：shortCode 直接填充为短链 URL
                response.setShortCode(shortLinkUrl);
                response.setShortLinkUrl(shortLinkUrl);
                response.setOriginalUrl(item.getOriginalUrl());
                responses.add(response);
            }

            return ApiResponse.ok(responses);
        } catch (Exception e) {
            log.error("批量创建短链失败", e);
            return ApiResponse.fail("批量创建短链失败: " + e.getMessage());
        }
    }

    // 说明：原有 “短码 -> 原始URL” 查询能力已废弃，当前仅保留通过 Short.io 生成外部可用短链URL的能力。
}

