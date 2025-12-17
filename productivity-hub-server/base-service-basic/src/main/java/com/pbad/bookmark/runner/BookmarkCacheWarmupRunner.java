package com.pbad.bookmark.runner;

import com.pbad.bookmark.service.BookmarkUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时预热宝藏网址缓存.
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class BookmarkCacheWarmupRunner implements ApplicationRunner {

    private final BookmarkUrlService bookmarkUrlService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            bookmarkUrlService.refreshBookmarkCache();
            log.info("[BookmarkWarmup] 宝藏网址缓存预热完成");
        } catch (Exception ex) {
            log.warn("[BookmarkWarmup] 预热宝藏网址缓存失败: {}", ex.getMessage(), ex);
        }
    }
}


