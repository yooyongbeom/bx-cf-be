package com.bwg.channel.backend.securitycommon.filter;

import com.bwg.channel.backend.securitycommon.constants.AuthErrorCode;
import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.securitycommon.session.SessionValidator;
import com.bwg.channel.backend.securitycommon.support.WebFluxAuthErrorResponseWriter;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class WebFluxJwtAuthFilter implements WebFilter {
    /** access token 검증과 claim 추출에 사용하는 JWT 유틸리티. */
    private final JwtUtil jwtUtil;

    /** 인증 없이 통과하는 화이트리스트(permitAll) 경로 매처. 매칭되면 토큰/세션 검증을 모두 건너뛴다. */
    private final ServerWebExchangeMatcher permitAllMatcher;

    /** access token의 sessionId가 유효한(로그인 상태) 세션인지 확인하는 포트. null이면 세션 검증을 하지 않는다. */
    private final SessionValidator sessionValidator;

    /** 세션 존재 검증 활성화 여부. false면 JWT 검증만 수행한다. */
    private final boolean sessionCheckEnabled;

    /** 세션 저장소 조회 실패(예: Redis 장애) 시 정책. true면 통과(fail-open), false면 거부(fail-closed). */
    private final boolean sessionCheckFailOpen;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // permitAll(화이트리스트) 경로는 토큰/세션 검증 없이 통과한다.
        // 이렇게 해야 refresh-token 같은 permitAll 경로에 (만료된) 토큰이 실려와도 선차단되지 않는다.
        return permitAllMatcher.matches(exchange)
                .flatMap(match -> match.isMatch()
                        ? chain.filter(exchange)
                        : authenticate(exchange, chain));
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, WebFilterChain chain) {
        // Authorization 헤더에서 Bearer access token 추출
        String token = resolveToken(exchange);

        // 토큰이 없으면 여기서 판단하지 않고, 뒤의 Spring Security 인가(authenticated)가 401 처리하도록 흘려보낸다.
        if (token == null) {
            return chain.filter(exchange);
        }

        try {
            // access token 서명/만료/type claim 검증
            jwtUtil.validateAccessToken(token);

            // 검증된 access token claim으로 SecurityContext 인증 객체 구성
            Authentication authentication = jwtUtil.getAuthenticationFromToken(token);

            // 내부 서비스가 JWT를 재해석하지 않도록 X-Auth-* 헤더 주입
            ServerWebExchange authenticatedExchange = addInternalAuthHeaders(exchange, authentication, token);

            // JWT는 유효하더라도 로그아웃 등으로 서버 세션이 제거됐으면 거부한다(access token 즉시 무효화).
            return verifySession(jwtUtil.getSessionId(token))
                    .flatMap(active -> active
                            ? chain.filter(authenticatedExchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                            : sendErrorResponse(exchange, AuthErrorCode.INVALID_TOKEN));
        } catch (ExpiredJwtException e) {
            return sendErrorResponse(exchange, AuthErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            return sendErrorResponse(exchange, AuthErrorCode.INVALID_TOKEN);
        }
    }

    private Mono<Boolean> verifySession(String sessionId) {
        // 세션 검증 비활성화이거나 구현체가 없으면 JWT 검증 결과를 그대로 신뢰한다.
        if (!sessionCheckEnabled || sessionValidator == null) {
            return Mono.just(true);
        }
        return sessionValidator.isActive(sessionId)
                .onErrorResume(e -> {
                    // 세션 저장소 조회 실패 시 정책에 따라 통과(fail-open)/거부(fail-closed)
                    log.warn("session validation failed (failOpen={}): {}", sessionCheckFailOpen, e.toString());
                    return Mono.just(sessionCheckFailOpen);
                });
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
