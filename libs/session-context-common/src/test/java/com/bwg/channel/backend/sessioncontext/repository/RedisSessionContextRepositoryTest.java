package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisSessionContextRepositoryTest {

    @Test
    void savesSessionContextBySessionKeyWithTtl() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, SessionContext> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        Duration ttl = Duration.ofHours(1);
        SessionContext context = SessionContext.builder()
                .sessionId("abc-123")
                .userId("user-1")
                .roles(List.of("ROLE_USER"))
                .loginTime(Instant.parse("2026-07-02T00:00:00Z"))
                .lastAccessTime(Instant.parse("2026-07-02T00:00:00Z"))
                .build();

        repository.save(context, ttl);

        verify(valueOperations).set("session:abc-123", context, ttl);
    }
}
