package common.web.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 熔断器实现.
 * <p>支持三种状态：关闭（CLOSED）、开启（OPEN）、半开（HALF_OPEN）</p>
 *
 * @author pbad
 */
@Slf4j
@Data
public class CircuitBreaker {

    /**
     * 熔断器状态
     */
    public enum State {
        CLOSED,   // 关闭：正常处理请求
        OPEN,     // 开启：拒绝请求，直接返回错误
        HALF_OPEN // 半开：尝试恢复，允许少量请求通过
    }

    private final String name;
    private final double errorThreshold;  // 错误率阈值
    private final long timeout;            // 超时时间（毫秒）
    private final int halfOpenRequests;   // 半开状态允许的请求数
    private final long openDuration;       // 开启状态持续时间（毫秒）

    private volatile State state = State.CLOSED;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong errorRequests = new AtomicLong(0);
    private final AtomicInteger halfOpenRequestCount = new AtomicInteger(0);
    private volatile long lastOpenTime = 0;

    public CircuitBreaker(String name, double errorThreshold, long timeout, int halfOpenRequests, long openDuration) {
        this.name = name;
        this.errorThreshold = errorThreshold;
        this.timeout = timeout;
        this.halfOpenRequests = halfOpenRequests;
        this.openDuration = openDuration;
    }

    /**
     * 检查是否允许请求通过.
     *
     * @return true表示允许，false表示拒绝
     */
    public boolean allowRequest() {
        State currentState = this.state;

        if (currentState == State.CLOSED) {
            return true;
        }

        if (currentState == State.OPEN) {
            // 检查是否可以进入半开状态
            if (System.currentTimeMillis() - lastOpenTime >= openDuration) {
                synchronized (this) {
                    if (this.state == State.OPEN) {
                        this.state = State.HALF_OPEN;
                        halfOpenRequestCount.set(0);
                        log.info("[CircuitBreaker] {} 从 OPEN 状态转为 HALF_OPEN 状态", name);
                    }
                }
                return true;
            }
            return false;
        }

        // HALF_OPEN 状态
        if (halfOpenRequestCount.get() < halfOpenRequests) {
            return true;
        }
        return false;
    }

    /**
     * 记录请求成功.
     */
    public void recordSuccess() {
        if (state == State.HALF_OPEN) {
            synchronized (this) {
                if (state == State.HALF_OPEN) {
                    // 半开状态下成功，关闭熔断器
                    this.state = State.CLOSED;
                    resetCounters();
                    log.info("[CircuitBreaker] {} 从 HALF_OPEN 状态转为 CLOSED 状态（恢复成功）", name);
                }
            }
        } else {
            totalRequests.incrementAndGet();
        }
    }

    /**
     * 记录请求失败.
     */
    public void recordFailure() {
        totalRequests.incrementAndGet();
        errorRequests.incrementAndGet();

        if (state == State.HALF_OPEN) {
            synchronized (this) {
                if (state == State.HALF_OPEN) {
                    // 半开状态下失败，重新开启熔断器
                    this.state = State.OPEN;
                    lastOpenTime = System.currentTimeMillis();
                    resetCounters();
                    log.warn("[CircuitBreaker] {} 从 HALF_OPEN 状态转为 OPEN 状态（恢复失败）", name);
                }
            }
        } else if (state == State.CLOSED) {
            // 检查是否需要开启熔断器
            checkAndOpen();
        }
    }

    /**
     * 记录超时.
     */
    public void recordTimeout() {
        recordFailure();
    }

    /**
     * 检查并开启熔断器.
     */
    private void checkAndOpen() {
        long total = totalRequests.get();
        long errors = errorRequests.get();

        if (total > 0) {
            double errorRate = (double) errors / total;
            if (errorRate >= errorThreshold) {
                synchronized (this) {
                    if (this.state == State.CLOSED) {
                        this.state = State.OPEN;
                        lastOpenTime = System.currentTimeMillis();
                        log.warn("[CircuitBreaker] {} 从 CLOSED 状态转为 OPEN 状态（错误率：{:.2f}%）", name, String.format("%.2f", errorRate * 100));
                    }
                }
            }
        }
    }

    /**
     * 重置计数器.
     */
    private void resetCounters() {
        totalRequests.set(0);
        errorRequests.set(0);
        halfOpenRequestCount.set(0);
    }

    /**
     * 在半开状态下增加请求计数.
     */
    public void incrementHalfOpenRequest() {
        if (state == State.HALF_OPEN) {
            halfOpenRequestCount.incrementAndGet();
        }
    }
}

