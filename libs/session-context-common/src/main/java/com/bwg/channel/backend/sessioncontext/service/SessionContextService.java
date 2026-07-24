package com.bwg.channel.backend.sessioncontext.service;

import com.bwg.channel.backend.sessioncontext.domain.SessionContext;

import java.time.Duration;
import java.util.Optional;

/**
 * 업무 서비스가 Redis/Valkey 내부 구현을 몰라도 세션 컨텍스트를 사용할 수 있게 하는 서비스 계약.
 */
public interface SessionContextService {
    /**
     * 로그인 또는 토큰 재발급 시 생성한 세션 컨텍스트를 refresh token 만료 정책에 맞춰 저장한다.
     *
     * @param context 저장할 사용자 세션 컨텍스트
     * @param ttl 세션을 유지할 기간
     */
    void save(SessionContext context, Duration ttl);

    /**
     * 내부 인증 헤더 또는 JWT claim에서 얻은 sessionId로 세션 컨텍스트를 조회한다.
     *
     * @param sessionId 조회할 세션 ID
     * @return 세션이 존재하면 해당 컨텍스트, 없으면 빈 Optional
     */
    Optional<SessionContext> findBySessionId(String sessionId);

    /**
     * sessionId에 해당하는 세션이 살아있는지만 확인한다.
     *
     * <p>게이트웨이가 access token 유효성(로그아웃 여부)을 매 요청마다 검증할 때 사용한다.</p>
     *
     * @param sessionId 존재 여부를 확인할 세션 ID
     * @return 세션이 존재하면 {@code true}
     */
    boolean existsBySessionId(String sessionId);

    /**
     * 로그아웃 또는 강제 만료 처리 시 sessionId 기준 세션 컨텍스트를 제거한다.
     *
     * @param sessionId 삭제할 세션 ID
     */
    void deleteBySessionId(String sessionId);

    /**
     * Deletes every session context owned by the specified user ID.
     *
     * @param userId user ID subject to forced logout
     */
    void deleteByUserId(String userId);

    /**
     * 삭제 작업 토큰으로 사용자별 세션 생성 차단 소유권을 획득한다.
     *
     * @param userId 신규 세션 발급을 차단할 사용자 ID
     * @param operationId tombstone 소유권을 식별하는 삭제 작업 토큰
     * @return 이 작업이 tombstone을 새로 획득했으면 {@code true}
     */
    boolean blockSessionCreation(String userId, String operationId);

    /**
     * 삭제 작업 토큰이 tombstone 소유자와 일치할 때만 사용자별 세션 생성 차단을 해제한다.
     *
     * @param userId 신규 세션 발급 차단을 해제할 사용자 ID
     * @param operationId tombstone을 획득한 삭제 작업 토큰
     */
    void unblockSessionCreation(String userId, String operationId);

    /**
     * 사용자별 신규 세션 생성 차단 여부를 조회한다.
     *
     * @param userId 차단 여부를 조회할 사용자 ID
     * @return tombstone이 존재하면 {@code true}
     */
    boolean isSessionCreationBlocked(String userId);
}
