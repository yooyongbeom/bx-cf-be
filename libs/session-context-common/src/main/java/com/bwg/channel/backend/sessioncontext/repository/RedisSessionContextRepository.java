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

    /** TTL 없이 사용자별 세션 생성 차단 tombstone을 저장한다. */
    private static final RedisScript<Long> BLOCK_SESSION_CREATION_SCRIPT = RedisScript.of("""
            redis.call('SET', KEYS[1], '1')
            return 1
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
    public void blockSessionCreation(String userId) {
        // 삭제가 완료된 뒤에도 유지되도록 만료 기간 없는 tombstone을 먼저 저장한다.
        redisTemplate.execute(
                BLOCK_SESSION_CREATION_SCRIPT,
                List.of(SessionContextKeys.userSessionBlockKey(userId))
        );
    }

    @Override
    public void unblockSessionCreation(String userId) {
        // 사용자 재등록 또는 삭제 실패 보상 시에만 해당 사용자의 tombstone을 제거한다.
        redisTemplate.delete(SessionContextKeys.userSessionBlockKey(userId));
    }
}
