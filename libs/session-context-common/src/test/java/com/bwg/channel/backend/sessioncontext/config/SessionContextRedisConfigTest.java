package com.bwg.channel.backend.sessioncontext.config;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class SessionContextRedisConfigTest {

    @Test
    void valueSerializerWritesSessionContextWithInstantFields() {
        RedisTemplate<String, SessionContext> redisTemplate =
                new SessionContextRedisConfig().sessionContextRedisTemplate(mock(RedisConnectionFactory.class));
        SessionContext context = SessionContext.builder()
                .sessionId("session-1")
                .userId("user-1")
                .roles(List.of("ROLE_USER"))
                .authLevel("LOGIN")
                .loginTime(Instant.parse("2026-07-02T08:54:35Z"))
                .lastAccessTime(Instant.parse("2026-07-02T08:54:35Z"))
                .build();

        @SuppressWarnings("unchecked")
        RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();

        assertThatCode(() -> valueSerializer.serialize(context))
                .doesNotThrowAnyException();
        Object deserialized = valueSerializer.deserialize(valueSerializer.serialize(context));
        assertThat(deserialized)
                .isInstanceOf(SessionContext.class)
                .extracting("loginTime", "lastAccessTime")
                .containsExactly(context.getLoginTime(), context.getLastAccessTime());
    }
}
