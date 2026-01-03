package common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redisConfig 配置类
 *
 * @author: pbad
 * @date: 2023/4/25 15:43
 * @version: 1.0
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义RedisTemplate.
     *
     * @param redisConnectionFactory redis连接工厂
     * @return 自定义RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        
        // 设置key序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        
        // 设置value序列化器 - 使用GenericJackson2JsonRedisSerializer保持兼容性
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        
        // 初始化RedisTemplate
        redisTemplate.afterPropertiesSet();
        
        return redisTemplate;
    }

}
