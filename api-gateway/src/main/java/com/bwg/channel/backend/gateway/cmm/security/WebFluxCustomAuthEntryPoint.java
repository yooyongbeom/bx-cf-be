package com.bwg.channel.backend.gateway.cmm.security;

import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// WebFlux 전용 인증 실패 처리
@Slf4j
@Component
public class WebFluxCustomAuthEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {

        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        log.warn("======> {}",  exchange.getRequest().getPath());

        ServerHttpResponse res = exchange.getResponse();
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        res.setStatusCode(HttpStatus.UNAUTHORIZED);

        String code = AuthErrorCode.UNAUTHORIZED_CLIENT.getCode();
        String msg = AuthErrorCode.UNAUTHORIZED_CLIENT.getMsg();

        String json = String.format("{\n"
                + "    \"success\": false,\n"
                + "    \"code\": \"%s\",\n"
                + "    \"msg\": \"%s\",\n"
                + "    \"payload\": { \n"
                + "         \"status\": %s,\n"
                + "         \"error\": \"Unauthorized\",\n"
                + "         \"message\": \"인증이 필요합니다.\",\n"
                + "         \"path\": \"%s\"\n"
                + "     } \n"
                + "}",code, msg, HttpStatus.UNAUTHORIZED.value(), exchange.getRequest().getPath());

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
    }
}