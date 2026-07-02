package com.bwg.channel.backend.securitycommon.filter;

import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.securitycommon.support.WebFluxAuthErrorResponseWriter;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class WebFluxJwtAuthFilter implements WebFilter {
    /** access token 검증과 claim 추출에 사용하는 JWT 유틸리티. */
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Authorization 헤더에서 Bearer access token 추출
        String token = resolveToken(exchange);

        if (token != null) {
            try {
                // access token 서명/만료/type claim 검증
                jwtUtil.validateAccessToken(token);

                // 검증된 access token claim으로 SecurityContext 인증 객체 구성
                Authentication authentication = jwtUtil.getAuthenticationFromToken(token);

                // 내부 서비스가 JWT를 재해석하지 않도록 X-Auth-* 헤더 주입
                ServerWebExchange authenticatedExchange = addInternalAuthHeaders(exchange, authentication, token);
                return chain.filter(authenticatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            } catch (ExpiredJwtException e) {
                return sendErrorResponse(exchange, AuthErrorCode.EXPIRED_TOKEN);
            } catch (JwtException | IllegalArgumentException e) {
                return sendErrorResponse(exchange, AuthErrorCode.INVALID_TOKEN);
            }
        }

        return chain.filter(exchange);
    }

    private String resolveToken(ServerWebExchange exchange) {
        // Gateway로 들어온 Authorization: Bearer ... 헤더 확인
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private ServerWebExchange addInternalAuthHeaders(
            ServerWebExchange exchange,
            Authentication authentication,
            String token
    ) {
        // 인증 객체의 권한 목록을 내부 전달용 콤마 문자열로 변환
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 클라이언트가 보낸 같은 이름의 헤더가 있어도 Gateway 검증 결과로 덮어쓰기
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.set(InternalAuthHeaders.USER, authentication.getName());
                    headers.set(InternalAuthHeaders.ROLES, roles);
                    headers.set(InternalAuthHeaders.SESSION_ID, jwtUtil.getSessionId(token));
                }))
                .build();
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, AuthErrorCode errorCode) {
        // WebFlux 응답 writer로 공통 인증 실패 JSON 생성
        return WebFluxAuthErrorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
