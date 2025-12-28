package com.pbad.thirdparty.api.shortlink.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 令牌桶限流器.
 * <p>实现简单的令牌桶算法，用于限制API调用频率.</p>
 *
 * @author pbad
 */
public class RateLimiter {

    private final long capacity; // 桶容量
    private final long refillRate; // 每秒补充的令牌数
    private final AtomicLong tokens; // 当前令牌数
    private final ReentrantLock lock = new ReentrantLock();
    private long lastRefillTime; // 上次补充令牌的时间

    /**
     * 创建限流器.
     *
     * @param capacity   桶容量（最大令牌数）
     * @param refillRate 每秒补充的令牌数
     */
    public RateLimiter(long capacity, long refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillTime = System.currentTimeMillis();
    }

    /**
     * 尝试获取令牌.
     *
     * @return 是否成功获取令牌
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量的令牌.
     *
     * @param requestedTokens 请求的令牌数
     * @return 是否成功获取令牌
     */
    public boolean tryAcquire(long requestedTokens) {
        if (requestedTokens <= 0) {
            return true;
        }
        if (requestedTokens > capacity) {
            return false;
        }

        lock.lock();
        try {
            refillTokens();
            long currentTokens = tokens.get();
            if (currentTokens >= requestedTokens) {
                tokens.addAndGet(-requestedTokens);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 补充令牌.
     */
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        
        if (elapsed > 0) {
            // 计算应该补充的令牌数
            long tokensToAdd = (elapsed * refillRate) / 1000;
            if (tokensToAdd > 0) {
                long currentTokens = tokens.get();
                long newTokens = Math.min(capacity, currentTokens + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
    }

    /**
     * 获取当前可用令牌数.
     *
     * @return 可用令牌数
     */
    public long getAvailableTokens() {
        lock.lock();
        try {
            refillTokens();
            return tokens.get();
        } finally {
            lock.unlock();
        }
    }
}

