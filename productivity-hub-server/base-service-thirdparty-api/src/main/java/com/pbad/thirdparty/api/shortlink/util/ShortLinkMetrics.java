package com.pbad.thirdparty.api.shortlink.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 短链服务监控指标.
 *
 * @author pbad
 */
public class ShortLinkMetrics {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong thirdPartySuccessCount = new AtomicLong(0);
    private final AtomicLong fallbackCount = new AtomicLong(0);
    private final AtomicLong rateLimitRejectedCount = new AtomicLong(0);

    /**
     * 记录请求.
     */
    public void recordRequest() {
        totalRequests.incrementAndGet();
    }

    /**
     * 记录成功.
     */
    public void recordSuccess() {
        successCount.incrementAndGet();
    }

    /**
     * 记录失败.
     */
    public void recordFailure() {
        failureCount.incrementAndGet();
    }

    /**
     * 记录第三方服务成功.
     */
    public void recordThirdPartySuccess() {
        thirdPartySuccessCount.incrementAndGet();
    }

    /**
     * 记录降级.
     */
    public void recordFallback() {
        fallbackCount.incrementAndGet();
    }

    /**
     * 记录限流拒绝.
     */
    public void recordRateLimitRejected() {
        rateLimitRejectedCount.incrementAndGet();
    }

    /**
     * 获取总请求数.
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * 获取成功数.
     */
    public long getSuccessCount() {
        return successCount.get();
    }

    /**
     * 获取失败数.
     */
    public long getFailureCount() {
        return failureCount.get();
    }

    /**
     * 获取第三方服务成功数.
     */
    public long getThirdPartySuccessCount() {
        return thirdPartySuccessCount.get();
    }

    /**
     * 获取降级数.
     */
    public long getFallbackCount() {
        return fallbackCount.get();
    }

    /**
     * 获取限流拒绝数.
     */
    public long getRateLimitRejectedCount() {
        return rateLimitRejectedCount.get();
    }

    /**
     * 获取成功率.
     */
    public double getSuccessRate() {
        long total = totalRequests.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successCount.get() / total * 100;
    }

    /**
     * 重置所有指标.
     */
    public void reset() {
        totalRequests.set(0);
        successCount.set(0);
        failureCount.set(0);
        thirdPartySuccessCount.set(0);
        fallbackCount.set(0);
        rateLimitRejectedCount.set(0);
    }
}

