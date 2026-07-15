package com.bwg.channel.backend.securitycommon.session;

import reactor.core.publisher.Mono;

/**
 * access token의 sessionId가 서버 측에서 여전히 유효한(로그인 상태) 세션인지 검증하는 포트.
 *
 * <p>security-common은 세션 저장소 구현(Redis 등)에 의존하지 않고 이 인터페이스만 안다.
 * 실제 구현체(예: Redis 세션 조회)는 이 필터를 사용하는 모듈(API Gateway)이 주입한다.
 * 블로킹 저장소 접근을 감싸 논블로킹 스케줄러로 오프로딩하는 책임도 구현체에 있다.</p>
 */
public interface SessionValidator {

    /**
     * sessionId가 가리키는 세션이 살아있으면 {@code true}를 방출한다.
     *
     * @param sessionId access token claim에서 추출한 세션 식별자
     * @return 세션 유효 여부. 저장소 장애 시의 처리(fail-open/closed)는 호출 측 정책에 위임한다.
     */
    Mono<Boolean> isActive(String sessionId);
}
