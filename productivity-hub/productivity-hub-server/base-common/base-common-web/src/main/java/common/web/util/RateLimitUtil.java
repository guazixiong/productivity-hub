package common.web.util;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 限流工具类.
 * <p>使用 Guava RateLimiter 实现令牌桶限流算法.</p>
 *
 * @author pbad
 */
@Slf4j
public class RateLimitUtil {

    /**
     * 用户限流器缓存（key: userId）
     */
    private static final ConcurrentMap<String, RateLimiter> USER_LIMITERS = new ConcurrentHashMap<>();

    /**
     * 接口限流器缓存（key: apiPath）
     */
    private static final ConcurrentMap<String, RateLimiter> API_LIMITERS = new ConcurrentHashMap<>();

    /**
     * IP限流器缓存（key: ip）
     */
    private static final ConcurrentMap<String, RateLimiter> IP_LIMITERS = new ConcurrentHashMap<>();

    /**
     * 尝试获取用户限流令牌.
     *
     * @param userId 用户ID
     * @param qps    QPS限制
     * @return 是否成功获取令牌
     */
    public static boolean tryAcquireForUser(String userId, double qps) {
        if (userId == null || userId.isEmpty()) {
            return true;
        }
        RateLimiter limiter = USER_LIMITERS.computeIfAbsent(userId, k -> RateLimiter.create(qps));
        return limiter.tryAcquire();
    }

    /**
     * 尝试获取接口限流令牌.
     *
     * @param apiPath 接口路径
     * @param qps     QPS限制
     * @return 是否成功获取令牌
     */
    public static boolean tryAcquireForApi(String apiPath, double qps) {
        if (apiPath == null || apiPath.isEmpty()) {
            return true;
        }
        RateLimiter limiter = API_LIMITERS.computeIfAbsent(apiPath, k -> RateLimiter.create(qps));
        return limiter.tryAcquire();
    }

    /**
     * 尝试获取IP限流令牌.
     *
     * @param ip  IP地址
     * @param qps QPS限制
     * @return 是否成功获取令牌
     */
    public static boolean tryAcquireForIp(String ip, double qps) {
        if (ip == null || ip.isEmpty()) {
            return true;
        }
        RateLimiter limiter = IP_LIMITERS.computeIfAbsent(ip, k -> RateLimiter.create(qps));
        return limiter.tryAcquire();
    }

    /**
     * 清理限流器缓存（用于动态调整QPS时重新创建限流器）.
     */
    public static void clearLimiters() {
        USER_LIMITERS.clear();
        API_LIMITERS.clear();
        IP_LIMITERS.clear();
        log.info("[RateLimitUtil] 已清理所有限流器缓存");
    }
}

