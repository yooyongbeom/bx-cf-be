package com.bwg.channel.backend.sessioncontext.service;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;

import java.time.Duration;
import java.util.Optional;

/**
 * 업무 서비스가 Redis/Valkey 내부 구현을 몰라도 세션 문맥을 사용할 수 있게 하는 서비스 계약.
 */
public interface SessionContextService {
    /**
     * 로그인 또는 토큰 재발급 시 생성한 세션 문맥을 refresh token 만료 정책에 맞춰 저장한다.
     */
    void save(SessionContext context, Duration ttl);

    /**
     * 내부 인증 헤더 또는 JWT claim에서 얻은 sessionId로 세션 문맥을 조회한다.
     */
    Optional<SessionContext> findBySessionId(String sessionId);

    /**
     * 로그아웃 또는 강제 만료 처리 시 sessionId 기준 세션 문맥을 제거한다.
     */
    void deleteBySessionId(String sessionId);
}
