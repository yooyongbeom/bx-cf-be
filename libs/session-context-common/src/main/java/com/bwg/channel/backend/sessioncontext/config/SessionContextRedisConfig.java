package com.bwg.channel.backend.sessioncontext.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * session-context-common에서 사용할 Redis key/value 직렬화 형식을 구성한다.
 */
@Configuration
public class SessionContextRedisConfig {

    @Bean
    public RedisTemplate<String, SessionContext> sessionContextRedisTemplate(
            RedisConnectionFactory redisConnectionFactory
    ) {
        // session:{sessionId} 문자열 key와 SessionContext JSON value 조합의 RedisTemplate
        RedisTemplate<String, SessionContext> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
        redisTemplate.setHashKeySerializer(StringRedisSerializer.UTF_8);
        GenericJackson2JsonRedisSerializer valueSerializer = sessionContextJsonSerializer();
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private GenericJackson2JsonRedisSerializer sessionContextJsonSerializer() {
        return new GenericJackson2JsonRedisSerializer()
                .configure(objectMapper -> objectMapper.registerModule(new JavaTimeModule()));
    }
}
