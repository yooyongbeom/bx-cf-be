package com.bwg.channel.backend.securitycommon.constants;

/**
 * Gateway가 검증한 인증 정보를 내부 서비스로 넘길 때 사용하는 신뢰 구간 전용 헤더 이름.
 */
public final class InternalAuthHeaders {
    /** JWT subject에서 추출한 사용자 ID 헤더. */
    public static final String USER = "X-Auth-User";

    /** JWT roles claim에서 추출한 권한 목록 헤더. */
    public static final String ROLES = "X-Auth-Roles";

    /** JWT sessionId claim에서 추출한 Redis 세션 식별자 헤더. */
    public static final String SESSION_ID = "X-Auth-Session-Id";

    private InternalAuthHeaders() {
    }
}
