package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.support.SessionContextKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis/Valkey 기반 세션 컨텍스트 저장소 구현체.
 */
@Repository
@RequiredArgsConstructor
public class RedisSessionContextRepository implements SessionContextRepository {
    /** session:{sessionId} key에 SessionContext 값을 저장하는 RedisTemplate. */
    private final RedisTemplate<String, SessionContext> redisTemplate;

    @Override
    public void save(SessionContext context, Duration ttl) {
        // access token claim의 sessionId와 같은 Redis key로 세션 컨텍스트 저장
        redisTemplate.opsForValue().set(SessionContextKeys.sessionKey(context.getSessionId()), context, ttl);
    }

    @Override
    public Optional<SessionContext> findBySessionId(String sessionId) {
        // 내부 서비스가 전달받은 X-Auth-Session-Id 기준으로 세션 컨텍스트 조회
        return Optional.ofNullable(redisTemplate.opsForValue().get(SessionContextKeys.sessionKey(sessionId)));
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        // 로그아웃 시 access token claim에서 전달된 sessionId 기준 Redis key 제거
        redisTemplate.delete(SessionContextKeys.sessionKey(sessionId));
    }
}
