package com.bwg.channel.backend.sessioncontext.support;

/**
 * Redis/Valkey에 저장되는 세션 컨텍스트 key 규칙을 한곳에서 관리한다.
 */
public final class SessionContextKeys {
    /** Gateway access token claim의 sessionId와 결합되는 Redis key 접두어. */
    private static final String SESSION_PREFIX = "session:";

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
}
