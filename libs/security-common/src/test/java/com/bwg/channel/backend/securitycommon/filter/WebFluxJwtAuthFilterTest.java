package com.bwg.channel.backend.securitycommon.filter;

import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.securitycommon.session.SessionValidator;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebFluxJwtAuthFilterTest {

    /** permitAll이 아닌(= 인증 필요) 경로에만 매칭되지 않는 매처. 실제 게이트웨이 화이트리스트를 대신한다. */
    private static final ServerWebExchangeMatcher PERMIT_ALL =
            ServerWebExchangeMatchers.pathMatchers("/channel/backend/api/v1/auth/login");

    private final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "hong.gildong",
            null,
            List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("MANAGER"))
    );

    @Test
    void authenticatedRequestForwardsInternalAuthHeaders() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionValidator sessionValidator = mock(SessionValidator.class);
        WebFluxJwtAuthFilter filter =
                new WebFluxJwtAuthFilter(jwtUtil, PERMIT_ALL, sessionValidator, true, false);
        String token = "access.jwt.token";

        when(jwtUtil.validateAccessToken(token)).thenReturn(true);
        when(jwtUtil.getAuthenticationFromToken(token)).thenReturn(authentication);
        when(jwtUtil.getSessionId(token)).thenReturn("session-123");
        when(sessionValidator.isActive("session-123")).thenReturn(Mono.just(true));

        MockServerHttpRequest request = MockServerHttpRequest.get("/channel/backend/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(InternalAuthHeaders.USER, "spoofed-user")
                .header(InternalAuthHeaders.ROLES, "SPOOFED")
                .header(InternalAuthHeaders.SESSION_ID, "spoofed-session")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();

        filter.filter(exchange, chainExchange -> {
            downstreamExchange.set(chainExchange);
            return Mono.empty();
        }).block();

        HttpHeaders headers = downstreamExchange.get().getRequest().getHeaders();
        assertThat(headers.getFirst(InternalAuthHeaders.USER)).isEqualTo("hong.gildong");
        assertThat(headers.getFirst(InternalAuthHeaders.ROLES)).isEqualTo("USER,MANAGER");
        assertThat(headers.getFirst(InternalAuthHeaders.SESSION_ID)).isEqualTo("session-123");
    }

    @Test
    void rejectsWhenSessionAlreadyRemovedByLogout() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionValidator sessionValidator = mock(SessionValidator.class);
        WebFluxJwtAuthFilter filter =
                new WebFluxJwtAuthFilter(jwtUtil, PERMIT_ALL, sessionValidator, true, false);
        String token = "access.jwt.token";

        when(jwtUtil.validateAccessToken(token)).thenReturn(true);
        when(jwtUtil.getAuthenticationFromToken(token)).thenReturn(authentication);
        when(jwtUtil.getSessionId(token)).thenReturn("session-123");
        // 로그아웃으로 세션이 제거된 상태
        when(sessionValidator.isActive("session-123")).thenReturn(Mono.just(false));

        MockServerHttpRequest request = MockServerHttpRequest.get("/channel/backend/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicBoolean downstreamCalled = new AtomicBoolean(false);

        filter.filter(exchange, chainExchange -> {
            downstreamCalled.set(true);
            return Mono.empty();
        }).block();

        // JWT는 유효하지만 세션이 없으므로 401로 차단되고 다운스트림까지 가지 않는다.
        assertThat(downstreamCalled).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void skipsTokenAndSessionCheckForPermitAllPath() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        SessionValidator sessionValidator = mock(SessionValidator.class);
        WebFluxJwtAuthFilter filter =
                new WebFluxJwtAuthFilter(jwtUtil, PERMIT_ALL, sessionValidator, true, false);

        // permitAll 경로(refresh 등)에 (만료/무효) 토큰이 실려와도 필터가 선차단하면 안 된다.
        MockServerHttpRequest request = MockServerHttpRequest.post("/channel/backend/api/v1/auth/login")
                .header(HttpHeaders.AUTHORIZATION, "Bearer stale.or.invalid.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        AtomicReference<ServerWebExchange> downstreamExchange = new AtomicReference<>();

        filter.filter(exchange, chainExchange -> {
            downstreamExchange.set(chainExchange);
            return Mono.empty();
        }).block();

        // 토큰 검증도, 세션 검증도 호출되지 않고 원본 그대로 통과한다.
        assertThat(downstreamExchange.get()).isSameAs(exchange);
        org.mockito.Mockito.verifyNoInteractions(jwtUtil, sessionValidator);
    }
}
