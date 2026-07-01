package com.bwg.channel.backend.gateway.cmm.security;

import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.support.WebFluxAuthErrorResponseWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebFluxCustomAccessDeniedHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
        log.warn("Forbidden access attempt: {}", ex.getMessage());

        return WebFluxAuthErrorResponseWriter.write(
                exchange,
                HttpStatus.FORBIDDEN,
                AuthErrorCode.ACCESS_DENIED
        );
    }
}
