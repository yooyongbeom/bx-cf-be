package com.bwg.channel.backend.sessioncontext.repository;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;

import java.time.Duration;
import java.util.Optional;

/**
 * 세션 컨텍스트 영속화 계약.
 *
 * <p>구현체는 Redis/Valkey 같은 내부 저장소의 key, TTL, 직렬화 방식을 캡슐화한다.</p>
 */
public interface SessionContextRepository {
    /**
     * 세션 컨텍스트를 TTL과 함께 저장한다.
     *
     * @param context 저장할 세션 컨텍스트
     * @param ttl refresh token 만료 정책과 맞춘 세션 유지 시간
     */
    void save(SessionContext context, Duration ttl);

    /**
     * 내부 인증 헤더의 sessionId 기준 세션 컨텍스트를 조회한다.
     */
    Optional<SessionContext> findBySessionId(String sessionId);

    /**
     * 로그아웃, 강제 만료 같은 이벤트에서 sessionId 기준 세션 컨텍스트를 제거한다.
     */
    void deleteBySessionId(String sessionId);
}
