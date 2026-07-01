package com.bwg.channel.backend.gateway.cmm.security;

import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.support.WebFluxAuthErrorResponseWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebFluxCustomAuthEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        log.warn("======> {}", exchange.getRequest().getPath());

        return WebFluxAuthErrorResponseWriter.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                AuthErrorCode.UNAUTHORIZED_CLIENT
        );
    }
}
