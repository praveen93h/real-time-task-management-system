package com.taskmanagement.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

        // Define cache-specific TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User cache - longer TTL as user data changes less frequently
        cacheConfigurations.put(CacheNames.USER_BY_ID, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put(CacheNames.USER_BY_USERNAME, defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Project cache - moderate TTL
        cacheConfigurations.put(CacheNames.PROJECT_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CacheNames.PROJECTS_BY_USER, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put(CacheNames.PROJECT_MEMBERS, defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Task cache - shorter TTL as tasks change frequently
        cacheConfigurations.put(CacheNames.TASKS_BY_PROJECT, defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put(CacheNames.TASK_BY_ID, defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * Cache name constants for consistency across services
     */
    public static class CacheNames {
        public static final String USER_BY_ID = "user:id";
        public static final String USER_BY_USERNAME = "user:username";
        public static final String PROJECT_BY_ID = "project:id";
        public static final String PROJECTS_BY_USER = "projects:user";
        public static final String PROJECT_MEMBERS = "project:members";
        public static final String TASKS_BY_PROJECT = "tasks:project";
        public static final String TASK_BY_ID = "task:id";
        
        private CacheNames() {}
    }
}
