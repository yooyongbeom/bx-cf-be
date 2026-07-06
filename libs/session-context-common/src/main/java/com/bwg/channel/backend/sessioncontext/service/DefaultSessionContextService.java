package com.bwg.channel.backend.sessioncontext.service;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;
import com.bwg.channel.backend.sessioncontext.repository.SessionContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * 업무 서비스가 저장소 구현체를 몰라도 Redis 세션 컨텍스트를 사용할 수 있게 하는 기본 서비스.
 *
 * <p>현재는 단순 위임 구조이며, 이후 lastAccessTime 갱신, 로그, TTL 재설정 같은 공통 정책을 이 계층에 둘 수 있다.</p>
 */
@Service
@RequiredArgsConstructor
public class DefaultSessionContextService implements SessionContextService {
    /** Redis/Valkey 저장 방식과 key 규칙을 캡슐화한 세션 컨텍스트 저장소. */
    private final SessionContextRepository sessionContextRepository;

    @Override
    public void save(SessionContext context, Duration ttl) {
        sessionContextRepository.save(context, ttl);
    }

    @Override
    public Optional<SessionContext> findBySessionId(String sessionId) {
        return sessionContextRepository.findBySessionId(sessionId);
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        sessionContextRepository.deleteBySessionId(sessionId);
    }
}
