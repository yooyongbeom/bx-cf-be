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

    /**
     * 세션 컨텍스트와 만료 기간을 저장소에 전달해 저장한다.
     *
     * @param context 저장할 사용자 세션 컨텍스트
     * @param ttl 세션을 유지할 기간
     */
    @Override
    public void save(SessionContext context, Duration ttl) {
        sessionContextRepository.save(context, ttl);
    }

    /**
     * 세션 ID에 해당하는 컨텍스트 조회를 저장소에 위임한다.
     *
     * @param sessionId 조회할 세션 ID
     * @return 세션이 존재하면 해당 컨텍스트, 없으면 빈 Optional
     */
    @Override
    public Optional<SessionContext> findBySessionId(String sessionId) {
        return sessionContextRepository.findBySessionId(sessionId);
    }

    /**
     * 세션 ID에 해당하는 컨텍스트의 존재 여부 확인을 저장소에 위임한다.
     *
     * @param sessionId 존재 여부를 확인할 세션 ID
     * @return 세션이 존재하면 {@code true}
     */
    @Override
    public boolean existsBySessionId(String sessionId) {
        return sessionContextRepository.existsBySessionId(sessionId);
    }

    /**
     * 세션 ID에 해당하는 컨텍스트 삭제를 저장소에 위임한다.
     *
     * @param sessionId 삭제할 세션 ID
     */
    @Override
    public void deleteBySessionId(String sessionId) {
        sessionContextRepository.deleteBySessionId(sessionId);
    }

    /**
     * Delegates user-ID-based forced logout to the repository implementation.
     *
     * @param userId user ID subject to forced logout
     */
    @Override
    public void deleteByUserId(String userId) {
        // The repository owns Redis key scanning and selective deletion details.
        sessionContextRepository.deleteByUserId(userId);
    }

    /**
     * 삭제 작업 토큰 기반 세션 생성 차단 획득을 저장소에 위임한다.
     *
     * @param userId 신규 세션 발급을 차단할 사용자 ID
     * @param operationId tombstone 소유권을 식별하는 삭제 작업 토큰
     * @return 이 작업이 tombstone을 새로 획득했으면 {@code true}
     */
    @Override
    public boolean blockSessionCreation(String userId, String operationId) {
        // tombstone의 SET NX 저장 방식과 소유권 판정은 저장소 구현체가 소유한다.
        return sessionContextRepository.blockSessionCreation(userId, operationId);
    }

    /**
     * 삭제 작업 토큰 기반 세션 생성 차단 해제를 저장소에 위임한다.
     *
     * @param userId 신규 세션 발급 차단을 해제할 사용자 ID
     * @param operationId tombstone을 획득한 삭제 작업 토큰
     */
    @Override
    public void unblockSessionCreation(String userId, String operationId) {
        // 다른 삭제 작업의 tombstone을 지우지 않도록 소유자 토큰을 그대로 전달한다.
        sessionContextRepository.unblockSessionCreation(userId, operationId);
    }

    /**
     * 사용자별 세션 생성 차단 여부 조회를 저장소에 위임한다.
     *
     * @param userId 차단 여부를 조회할 사용자 ID
     * @return tombstone이 존재하면 {@code true}
     */
    @Override
    public boolean isSessionCreationBlocked(String userId) {
        // marker 값은 불투명하게 유지하고 key 존재 여부만 업무 계층에 제공한다.
        return sessionContextRepository.isSessionCreationBlocked(userId);
    }
}
