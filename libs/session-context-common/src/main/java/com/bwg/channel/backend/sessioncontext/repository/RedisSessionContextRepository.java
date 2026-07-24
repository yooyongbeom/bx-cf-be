package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.exception.SessionCreationBlockedException;
import com.bwg.channel.backend.sessioncontext.support.SessionContextKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Redis/Valkey 기반 세션 컨텍스트 저장소 구현체.
 */
@Repository
@RequiredArgsConstructor
public class RedisSessionContextRepository implements SessionContextRepository {
    /**
     * tombstone 확인과 세션 저장을 한 Redis 명령 단위로 묶어 삭제와 로그인 경합을 차단한다.
     */
    private static final RedisScript<Long> SAVE_SESSION_IF_ALLOWED_SCRIPT = RedisScript.of("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return 0
            end
            redis.call('PSETEX', KEYS[2], ARGV[1], ARGV[2])
            return 1
            """, Long.class);

    /** 삭제 작업 토큰을 값으로 사용해 TTL 없는 tombstone을 최초 작업만 획득한다. */
    private static final RedisScript<Long> BLOCK_SESSION_CREATION_SCRIPT = RedisScript.of("""
            local acquired = redis.call('SET', KEYS[1], ARGV[1], 'NX')
            if acquired then
                return 1
            end
            return 0
            """, Long.class);

    /** 저장된 작업 토큰이 일치할 때만 tombstone을 원자적으로 제거한다. */
    private static final RedisScript<Long> UNBLOCK_SESSION_CREATION_SCRIPT = RedisScript.of("""
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    /** session:{sessionId} key에 SessionContext 값을 저장하는 RedisTemplate. */
    private final RedisTemplate<String, SessionContext> redisTemplate;

    @Override
    public void save(SessionContext context, Duration ttl) {
        // tombstone 확인과 session:{sessionId} PSETEX를 Lua로 원자 실행한다.
        Long saved = redisTemplate.execute(
                SAVE_SESSION_IF_ALLOWED_SCRIPT,
                List.of(
                        SessionContextKeys.userSessionBlockKey(context.getUserId()),
                        SessionContextKeys.sessionKey(context.getSessionId())
                ),
                ttl.toMillis(),
                context
        );
        if (!Long.valueOf(1L).equals(saved)) {
            // tombstone이 존재하면 세션을 만들지 않고 인증 서비스가 변환할 전용 원인을 전달한다.
            throw new SessionCreationBlockedException(context.getUserId());
        }
    }

    @Override
    public Optional<SessionContext> findBySessionId(String sessionId) {
        // 내부 서비스가 전달받은 X-Auth-Session-Id 기준으로 세션 컨텍스트 조회
        return Optional.ofNullable(redisTemplate.opsForValue().get(SessionContextKeys.sessionKey(sessionId)));
    }

    @Override
    public boolean existsBySessionId(String sessionId) {
        // 값 역직렬화 없이 session:{sessionId} key 존재 여부만 확인 (매 요청 호출 대비 경량)
        return Boolean.TRUE.equals(redisTemplate.hasKey(SessionContextKeys.sessionKey(sessionId)));
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        // 로그아웃 시 access token claim에서 전달된 sessionId 기준 Redis key 제거
        redisTemplate.delete(SessionContextKeys.sessionKey(sessionId));
    }

    @Override
    public void deleteByUserId(String userId) {
        // SCAN avoids the blocking KEYS command while traversing session keys in production Redis.
        ScanOptions options = ScanOptions.scanOptions()
                .match(SessionContextKeys.sessionPattern())
                .count(100)
                .build();
        List<String> ownedSessionKeys = new ArrayList<>();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String sessionKey = cursor.next();
                SessionContext context = redisTemplate.opsForValue().get(sessionKey);
                if (context != null && userId.equals(context.getUserId())) {
                    ownedSessionKeys.add(sessionKey);
                }
            }
        }

        // Invoke bulk deletion only when the target user has at least one active session.
        if (!ownedSessionKeys.isEmpty()) {
            redisTemplate.delete(ownedSessionKeys);
        }
    }

    @Override
    public boolean blockSessionCreation(String userId, String operationId) {
        // SET NX로 기존 삭제 작업의 소유권을 덮어쓰지 않고 신규 획득 여부를 반환한다.
        Long acquired = redisTemplate.execute(
                BLOCK_SESSION_CREATION_SCRIPT,
                List.of(SessionContextKeys.userSessionBlockKey(userId)),
                operationId
        );
        return Long.valueOf(1L).equals(acquired);
    }

    @Override
    public void unblockSessionCreation(String userId, String operationId) {
        // 비교와 삭제를 한 Lua 명령으로 실행해 다른 작업이 소유한 tombstone을 보존한다.
        redisTemplate.execute(
                UNBLOCK_SESSION_CREATION_SCRIPT,
                List.of(SessionContextKeys.userSessionBlockKey(userId)),
                operationId
        );
    }

    @Override
    public boolean isSessionCreationBlocked(String userId) {
        // marker 값은 삭제 작업 소유권에만 사용하므로 조회 시 역직렬화하지 않는다.
        return Boolean.TRUE.equals(redisTemplate.hasKey(SessionContextKeys.userSessionBlockKey(userId)));
    }
}
