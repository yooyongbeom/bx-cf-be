package com.bwg.channel.backend.authcore.filter;

import com.bwg.channel.backend.authcore.util.JwtUtil;
import com.bwg.channel.backend.common.constants.enums.AuthErrorCode;
import com.bwg.channel.backend.common.domain.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class WebFluxJwtAuthFilter implements WebFilter {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange);

        if (token != null) {
            try {
                // 1. 토큰 유효성 검증
                jwtUtil.validateToken(token);

                // 2. 토큰이 유효하면 인증 객체를 السياق الأمني (Security Context)에 저장
                Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            } catch (ExpiredJwtException e) {
                // 토큰 만료 시
                return sendErrorResponse(exchange, AuthErrorCode.EXPIRED_TOKEN);
            } catch (JwtException | IllegalArgumentException e) {
                // 그 외 유효하지 않은 토큰
                return sendErrorResponse(exchange, AuthErrorCode.INVALID_TOKEN);
            }
        }

        return chain.filter(exchange);
    }

    private String resolveToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, AuthErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<?> apiResponse = ApiResponse.fail(errorCode.getCode(), errorCode.getMsg());
        byte[] responseBytes;
        try {
            responseBytes = objectMapper.writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            // 직렬화 실패 시, 간단한 에러 응답
            responseBytes = "{\"code\":\"-9999\",\"message\":\"Error response serialization failed\"}".getBytes();
        }

        DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
        return response.writeWith(Mono.just(buffer));
    }
}