package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.exception.SessionCreationBlockedException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisSessionContextRepositoryTest {

    @Test
    void atomicallySavesSessionWhenUserHasNoCreationBlock() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        Duration ttl = Duration.ofHours(1);
        SessionContext context = SessionContext.builder()
                .sessionId("abc-123")
                .userId("user-1")
                .roles(List.of("ROLE_USER"))
                .loginTime(Instant.parse("2026-07-02T00:00:00Z"))
                .lastAccessTime(Instant.parse("2026-07-02T00:00:00Z"))
                .build();
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:user-1", "session:abc-123")),
                eq(ttl.toMillis()),
                eq(context)
        )).thenReturn(1L);

        repository.save(context, ttl);

        // tombstone 확인과 PSETEX가 하나의 Lua 실행 안에서 처리되어야 한다.
        verify(redisTemplate).execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:user-1", "session:abc-123")),
                eq(ttl.toMillis()),
                eq(context)
        );
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void rejectsAtomicSaveWhenUserSessionCreationIsBlocked() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        Duration ttl = Duration.ofHours(1);
        SessionContext context = SessionContext.builder()
                .sessionId("blocked-session")
                .userId("blocked-user")
                .build();
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:blocked-user", "session:blocked-session")),
                eq(ttl.toMillis()),
                eq(context)
        )).thenReturn(0L);

        assertThatThrownBy(() -> repository.save(context, ttl))
                .isInstanceOf(SessionCreationBlockedException.class);

        verify(redisTemplate, never()).opsForValue();
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

    @Test
    void acquiresSessionCreationBlockWithOperationTokenUsingSetNx() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:target-user")),
                eq("delete-operation-1")
        )).thenReturn(1L);

        boolean acquired = repository.blockSessionCreation("target-user", "delete-operation-1");

        assertThat(acquired).isTrue();
        ArgumentCaptor<RedisScript<Long>> scriptCaptor = redisScriptCaptor();
        verify(redisTemplate).execute(
                scriptCaptor.capture(),
                eq(List.of("session-blocked:target-user")),
                eq("delete-operation-1")
        );
        // 삭제 작업 토큰을 값으로 저장하고 NX로 기존 소유자의 tombstone을 덮어쓰지 않아야 한다.
        assertThat(normalizeScript(scriptCaptor.getValue()))
                .contains("redis.call('SET', KEYS[1], ARGV[1], 'NX')");
    }

    @Test
    void reportsSessionCreationBlockAsNotAcquiredWhenSetNxFindsExistingOwner() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:target-user")),
                eq("delete-operation-2")
        )).thenReturn(0L);

        boolean acquired = repository.blockSessionCreation("target-user", "delete-operation-2");

        assertThat(acquired).isFalse();
    }

    @Test
    void unblocksSessionCreationOnlyWhenOperationTokenOwnsTheTombstone() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        when(redisTemplate.execute(
                org.mockito.ArgumentMatchers.<RedisScript<Long>>any(),
                eq(List.of("session-blocked:target-user")),
                eq("delete-operation-owner")
        )).thenReturn(1L);

        repository.unblockSessionCreation("target-user", "delete-operation-owner");

        ArgumentCaptor<RedisScript<Long>> scriptCaptor = redisScriptCaptor();
        verify(redisTemplate).execute(
                scriptCaptor.capture(),
                eq(List.of("session-blocked:target-user")),
                eq("delete-operation-owner")
        );
        String script = normalizeScript(scriptCaptor.getValue());
        // GET 비교와 DEL을 한 Lua 실행으로 묶어 다른 삭제 작업이 소유한 tombstone을 보존한다.
        assertThat(script)
                .contains("if redis.call('GET', KEYS[1]) == ARGV[1] then")
                .contains("redis.call('DEL', KEYS[1])");
        verify(redisTemplate, never()).delete("session-blocked:target-user");
    }

    @Test
    void reportsWhetherSessionCreationIsBlockedWithoutReadingTheOpaqueToken() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, SessionContext> redisTemplate = mock(RedisTemplate.class);
        RedisSessionContextRepository repository = new RedisSessionContextRepository(redisTemplate);
        when(redisTemplate.hasKey("session-blocked:target-user")).thenReturn(true, false);

        assertThat(repository.isSessionCreationBlocked("target-user")).isTrue();
        assertThat(repository.isSessionCreationBlocked("target-user")).isFalse();

        // 차단 조회는 소유권 토큰을 역직렬화하지 않고 key 존재 여부만 사용한다.
        verify(redisTemplate, org.mockito.Mockito.times(2)).hasKey("session-blocked:target-user");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentCaptor<RedisScript<Long>> redisScriptCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(RedisScript.class);
    }

    private String normalizeScript(RedisScript<Long> script) {
        return script.getScriptAsString().replaceAll("\\s+", " ").trim();
    }
}
