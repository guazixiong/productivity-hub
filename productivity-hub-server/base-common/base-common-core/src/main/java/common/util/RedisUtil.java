package common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * redis 工具类.
 *
 * @author: pbad
 * @date: 2023/4/25 15:48
 * @version: 1.0
 */
@Component
public class RedisUtil {

    /**
     * 默认过期时间
     */
    private static final long DEFAULT_EXPIRATION_TIME = 3600;

    /**
     * 默认时间单位
     */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置key-value
     *
     * @param key   键值
     * @param value value值
     */
    public void defaultSetKeyNoExpiration(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置key-value对，并设置过期时间
     *
     * @param key   键值
     * @param value value值
     */
    public void defaultSetKey(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRATION_TIME, DEFAULT_TIME_UNIT);
    }

    /**
     * 设置key-value对，并设置过期时间
     *
     * @param key      键值
     * @param value    value值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void setKey(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 获取key对应的value.
     *
     * @param key 键值
     * @return value值
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取key对应的原始字符串值（不进行反序列化）.
     * 用于处理序列化异常的情况，可以手动解析JSON
     *
     * @param key 键值
     * @return 原始字符串值，如果key不存在或获取失败则返回null
     */
    public String getRawStringValue(String key) {
        try {
            @SuppressWarnings("unchecked")
            org.springframework.data.redis.serializer.RedisSerializer<String> keySerializer = 
                (org.springframework.data.redis.serializer.RedisSerializer<String>) redisTemplate.getKeySerializer();
            byte[] keyBytes = keySerializer.serialize(key);
            if (keyBytes == null) {
                return null;
            }
            byte[] rawBytes = redisTemplate.execute((RedisCallback<byte[]>) connection -> {
                return connection.get(keyBytes);
            });
            if (rawBytes != null) {
                return new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // 忽略异常，返回null
        }
        return null;
    }

    /**
     * 判断key是否存在.
     *
     * @param key 键值
     * @return true/false
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除key-value对.
     *
     * @param key 键值
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 判断key是否已经过期.
     *
     * @param key 键值
     * @return true/false
     */
    public Boolean isExpired(String key) {
        return redisTemplate.getExpire(key) <= 0;
    }

    /**
     * 刷新key的过期时间.
     *
     * @param key 键值
     * @param timeout 超时时间
     * @param timeUnit 超时单位
     */
    public void refreshExpiration(String key, long timeout, TimeUnit timeUnit) {
        redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 递增操作（原子操作）
     *
     * @param key 键值
     * @return 递增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增操作（原子操作），并设置过期时间
     *
     * @param key 键值
     * @param timeout 过期时间
     * @param timeUnit 时间单位
     * @return 递增后的值
     */
    public Long increment(String key, long timeout, TimeUnit timeUnit) {
        Long value = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, timeout, timeUnit);
        return value;
    }

    /**
     * 获取所有匹配的key
     *
     * @param pattern 匹配模式
     * @return key集合
     */
    public java.util.Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 如果key不存在则设置（SETNX操作，原子性）
     *
     * @param key      键值
     * @param value    value值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return true表示设置成功（key不存在），false表示key已存在
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }
}
