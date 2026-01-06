package com.pbad.monitor.service.impl;

import com.pbad.monitor.domain.vo.*;
import com.pbad.monitor.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控服务实现
 * TASK-BACKEND-10: 监控与告警实现
 *
 * @author: pbad
 * @date: 2025-01-XX
 */
@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    // 应用指标统计（简单实现，生产环境建议使用Micrometer）
    private final Map<String, AtomicLong> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> responseTimes = new ConcurrentHashMap<>();

    @Override
    public SystemMetricsVO getSystemMetrics() {
        SystemMetricsVO metrics = new SystemMetricsVO();
        List<MetricVO> metricList = new ArrayList<>();

        try {
            // CPU使用率
            Double cpuUsage = getCpuUsage();
            metrics.setCpuUsage(cpuUsage);
            metricList.add(createMetric("cpu_usage", cpuUsage, "%", "CPU使用率"));

            // 内存使用情况
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            long heapUsed = heapMemory.getUsed() / 1024 / 1024; // MB
            long heapTotal = heapMemory.getMax() / 1024 / 1024; // MB
            long nonHeapUsed = nonHeapMemory.getUsed() / 1024 / 1024; // MB

            metrics.setHeapMemoryUsed(heapUsed);
            metrics.setHeapMemoryTotal(heapTotal);
            metrics.setNonHeapMemoryUsed(nonHeapUsed);

            double memoryUsage = heapTotal > 0 ? (double) heapUsed / heapTotal * 100 : 0;
            metrics.setMemoryUsage(memoryUsage);
            metricList.add(createMetric("memory_usage", memoryUsage, "%", "内存使用率"));
            metricList.add(createMetric("heap_memory_used", (double) heapUsed, "MB", "堆内存使用"));
            metricList.add(createMetric("heap_memory_total", (double) heapTotal, "MB", "堆内存总量"));

            // 线程信息
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadBean.getThreadCount();
            int activeThreadCount = threadBean.getDaemonThreadCount();

            metrics.setThreadCount(threadCount);
            metrics.setActiveThreadCount(activeThreadCount);
            metricList.add(createMetric("thread_count", (double) threadCount, "个", "线程数"));

            // JVM运行时间
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000; // 秒
            metrics.setUptime(uptime);
            metricList.add(createMetric("uptime", (double) uptime, "秒", "JVM运行时间"));

            // 磁盘使用率（简化实现，实际应该读取文件系统信息）
            metrics.setDiskUsage(0.0); // 需要根据实际情况实现
            metricList.add(createMetric("disk_usage", 0.0, "%", "磁盘使用率"));

        } catch (Exception e) {
            log.error("获取系统监控指标失败", e);
        }

        metrics.setMetrics(metricList);
        return metrics;
    }

    @Override
    public ApplicationMetricsVO getApplicationMetrics() {
        ApplicationMetricsVO metrics = new ApplicationMetricsVO();
        List<MetricVO> metricList = new ArrayList<>();
        Map<String, ApiStatVO> apiStats = new HashMap<>();

        try {
            long totalRequests = 0;
            long successRequests = 0;
            long errorRequests = 0;
            long totalResponseTime = 0;
            int responseTimeCount = 0;
            long maxResponseTime = 0;
            long minResponseTime = Long.MAX_VALUE;

            // 统计所有接口
            for (Map.Entry<String, AtomicLong> entry : requestCounts.entrySet()) {
                String path = entry.getKey();
                long requests = entry.getValue().get();
                long success = successCounts.getOrDefault(path, new AtomicLong(0)).get();
                long errors = errorCounts.getOrDefault(path, new AtomicLong(0)).get();

                totalRequests += requests;
                successRequests += success;
                errorRequests += errors;

                // 计算响应时间统计
                List<Long> times = responseTimes.getOrDefault(path, new ArrayList<>());
                if (!times.isEmpty()) {
                    ApiStatVO apiStat = new ApiStatVO();
                    apiStat.setPath(path);
                    apiStat.setRequestCount(requests);
                    apiStat.setSuccessCount(success);
                    apiStat.setErrorCount(errors);

                    long sum = times.stream().mapToLong(Long::longValue).sum();
                    long avg = sum / times.size();
                    long max = times.stream().mapToLong(Long::longValue).max().orElse(0);
                    long min = times.stream().mapToLong(Long::longValue).min().orElse(0);

                    apiStat.setAvgResponseTime((double) avg);
                    apiStat.setMaxResponseTime((double) max);

                    apiStats.put(path, apiStat);

                    totalResponseTime += sum;
                    responseTimeCount += times.size();
                    maxResponseTime = Math.max(maxResponseTime, max);
                    minResponseTime = Math.min(minResponseTime, min);
                }
            }

            metrics.setTotalRequests(totalRequests);
            metrics.setSuccessRequests(successRequests);
            metrics.setErrorRequests(errorRequests);

            double errorRate = totalRequests > 0 ? (double) errorRequests / totalRequests * 100 : 0;
            metrics.setErrorRate(errorRate);
            metricList.add(createMetric("error_rate", errorRate, "%", "错误率"));

            if (responseTimeCount > 0) {
                double avgResponseTime = (double) totalResponseTime / responseTimeCount;
                metrics.setAvgResponseTime(avgResponseTime);
                metrics.setMaxResponseTime((double) maxResponseTime);
                metrics.setMinResponseTime(minResponseTime == Long.MAX_VALUE ? 0.0 : (double) minResponseTime);
                metricList.add(createMetric("avg_response_time", avgResponseTime, "ms", "平均响应时间"));
            }

            // QPS计算（简化实现，实际应该基于时间窗口）
            metrics.setQps(0.0);
            metricList.add(createMetric("qps", 0.0, "req/s", "每秒请求数"));

            metrics.setApiStats(apiStats);
            metrics.setMetrics(metricList);

        } catch (Exception e) {
            log.error("获取应用监控指标失败", e);
        }

        return metrics;
    }

    /**
     * 记录请求
     */
    public void recordRequest(String path, boolean success, long responseTime) {
        requestCounts.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
        if (success) {
            successCounts.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
        } else {
            errorCounts.computeIfAbsent(path, k -> new AtomicLong(0)).incrementAndGet();
        }

        responseTimes.computeIfAbsent(path, k -> new ArrayList<>()).add(responseTime);
        // 只保留最近1000条记录
        List<Long> times = responseTimes.get(path);
        if (times.size() > 1000) {
            times.remove(0);
        }
    }

    /**
     * 获取CPU使用率
     */
    private Double getCpuUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad() * 100;
        } catch (Exception e) {
            log.warn("获取CPU使用率失败", e);
            return 0.0;
        }
    }

    /**
     * 创建指标对象
     */
    private MetricVO createMetric(String name, Double value, String unit, String description) {
        MetricVO metric = new MetricVO();
        metric.setName(name);
        metric.setValue(value);
        metric.setUnit(unit);
        metric.setDescription(description);
        metric.setTimestamp(LocalDateTime.now());
        return metric;
    }
}

