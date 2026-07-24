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
     * sessionId에 해당하는 세션이 살아있는지만 확인한다.
     *
     * <p>값 역직렬화 없이 key 존재 여부만 보므로, 게이트웨이가 매 요청마다 호출하는
     * 로그인 상태(access token 유효성) 검증 용도에 적합하다.</p>
     */
    boolean existsBySessionId(String sessionId);

    /**
     * 로그아웃, 강제 만료 같은 이벤트에서 sessionId 기준 세션 컨텍스트를 제거한다.
     */
    void deleteBySessionId(String sessionId);

    /**
     * Deletes every session context owned by the specified user ID.
     *
     * @param userId user ID subject to forced logout
     */
    void deleteByUserId(String userId);

    /**
     * 삭제 작업 토큰을 소유자로 하는 세션 생성 차단 tombstone을 최초 한 번만 저장한다.
     *
     * @param userId 신규 세션 발급을 차단할 사용자 ID
     * @param operationId tombstone 소유권을 식별하는 삭제 작업 토큰
     * @return 이 작업이 tombstone을 새로 획득했으면 {@code true}
     */
    boolean blockSessionCreation(String userId, String operationId);

    /**
     * 저장된 소유자 토큰이 일치할 때만 세션 생성 차단 tombstone을 제거한다.
     *
     * @param userId 신규 세션 발급 차단을 해제할 사용자 ID
     * @param operationId tombstone을 획득한 삭제 작업 토큰
     */
    void unblockSessionCreation(String userId, String operationId);

    /**
     * 사용자별 신규 세션 생성 차단 tombstone 존재 여부를 조회한다.
     *
     * @param userId 차단 여부를 조회할 사용자 ID
     * @return tombstone이 존재하면 {@code true}
     */
    boolean isSessionCreationBlocked(String userId);
}
