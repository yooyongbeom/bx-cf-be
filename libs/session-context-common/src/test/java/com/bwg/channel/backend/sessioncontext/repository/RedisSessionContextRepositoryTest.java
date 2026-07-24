package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Test
    void deletesOnlySessionsOwnedByRequestedUser() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, SessionContext> valueOperations = mock(ValueOperations.class);
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = mock(Cursor.class);

        SessionContext target = SessionContext.builder()
                .sessionId("target-session")
                .userId("target-user")
                .build();
        SessionContext other = SessionContext.builder()
                .sessionId("other-session")
                .userId("other-user")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("session:target-session", "session:other-session");
        when(valueOperations.get("session:target-session")).thenReturn(target);
        when(valueOperations.get("session:other-session")).thenReturn(other);

        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        repository.deleteByUserId("target-user");

        verify(redisTemplate).delete(List.of("session:target-session"));
        verify(redisTemplate, never()).delete("session:other-session");
    }
}
