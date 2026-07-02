package com.bwg.channel.backend.sessioncontext.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Gateway 인증 이후 여러 서비스가 공유하는 로그인 세션 컨텍스트.
 *
 * <p>WAS 로컬 세션 대신 Redis/Valkey에 저장되는 값이며, 내부 서비스는 sessionId 기준으로 사용자,
 * 권한, 인증 수준 같은 요청 처리 문맥을 조회한다.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionContext implements Serializable {
    /** Redis key의 일부로 사용하는 세션 ID, access token의 sessionId claim과 같은 값. */
    private String sessionId;

    /** JWT subject와 내부 서비스의 사용자 식별에 사용하는 사용자 ID. */
    private String userId;

    /** Gateway 또는 업무 서비스가 권한 판단에 참고할 권한 목록. */
    private List<String> roles;

    /** MFA나 추가 인증이 들어왔을 때 로그인 세션의 인증 강도를 표현할 값. */
    private String authLevel;

    /** 로그인 성공 시 Redis 세션이 생성된 시각. */
    private Instant loginTime;

    /** 세션 컨텍스트가 마지막으로 갱신되거나 사용된 시각. */
    private Instant lastAccessTime;
}
