package com.bwg.channel.backend.gateway.config;

import com.bwg.channel.backend.securitycommon.session.SessionValidator;
import com.bwg.channel.backend.sessioncontext.service.SessionContextService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 게이트웨이에서 security-common의 {@link SessionValidator} 포트를 Redis 세션 저장소 구현으로 배선한다.
 *
 * <p>{@link SessionContextService#existsBySessionId(String)}는 블로킹 {@code RedisTemplate} 호출이므로,
 * WebFlux 이벤트 루프를 막지 않도록 {@link Schedulers#boundedElastic()}로 오프로딩한다.</p>
 */
@Configuration
public class SessionValidatorConfig {

    @Bean
    public SessionValidator sessionValidator(SessionContextService sessionContextService) {
        return sessionId -> Mono
                .fromCallable(() -> sessionContextService.existsBySessionId(sessionId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
