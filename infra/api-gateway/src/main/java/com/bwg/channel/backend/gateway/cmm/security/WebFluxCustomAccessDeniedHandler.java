package com.bwg.channel.backend.gateway.cmm.security;

import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

//  WebFlux 전용 권한 없음(403) 처리 핸들러
@Slf4j
@Component
public class WebFluxCustomAccessDeniedHandler implements ServerAccessDeniedHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange,
                             AccessDeniedException ex) {

        log.warn("Forbidden access attempt: {}", ex.getMessage());
        ServerHttpResponse res = exchange.getResponse();

        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        res.setStatusCode(HttpStatus.FORBIDDEN);

        String code = AuthErrorCode.ACCESS_DENIED.getCode();
        String msg = AuthErrorCode.ACCESS_DENIED.getMsg();

        String json = String.format("{\n"
                + "    \"success\": false,\n"
                + "    \"code\": \"%s\",\n"
                + "    \"msg\": \"%s\",\n"
                + "    \"payload\": { \n"
                + "         \"status\": %s,\n"
                + "         \"error\": \"Unauthorized\",\n"
                + "         \"message\": \"권한이 없습니다.\",\n"
                + "         \"path\": \"%s\"\n"
                + "     } \n"
                + "}",code, msg, HttpStatus.FORBIDDEN.value(), exchange.getRequest().getPath());

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
    }
}