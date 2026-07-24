package com.bwg.channel.backend.sessioncontext.support;

/**
 * Redis/Valkey에 저장되는 세션 컨텍스트 key 규칙을 한곳에서 관리한다.
 */
public final class SessionContextKeys {
    /** Gateway access token claim의 sessionId와 결합되는 Redis key 접두어. */
    private static final String SESSION_PREFIX = "session:";
    /** 사용자 삭제 경합 중 신규 세션 발급을 차단하는 tombstone key 접두어. */
    private static final String USER_SESSION_BLOCK_PREFIX = "session-blocked:";

    private SessionContextKeys() {
    }

    /**
     * 내부 인증 헤더나 JWT claim에서 전달된 sessionId를 Redis 저장 key로 변환한다.
     *
     * @param sessionId JWT claim 또는 내부 헤더로 전달되는 세션 ID
     * @return Redis/Valkey 저장 key
     */
    public static String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    /**
     * Returns the Redis SCAN pattern that selects only session-context keys.
     *
     * @return pattern matching session-context keys only
     */
    public static String sessionPattern() {
        return SESSION_PREFIX + "*";
    }

    /**
     * 사용자별 세션 생성 차단 tombstone key를 반환한다.
     *
     * <p>세션 탐색 패턴과 겹치지 않는 별도 namespace를 사용해 강제 로그아웃 SCAN에서 제외한다.</p>
     *
     * @param userId 신규 세션 발급을 차단할 사용자 ID
     * @return 사용자별 세션 생성 차단 key
     */
    public static String userSessionBlockKey(String userId) {
        return USER_SESSION_BLOCK_PREFIX + userId;
    }
}
