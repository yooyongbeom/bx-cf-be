package com.bwg.channel.backend.securitycommon.filter;

import com.bwg.channel.backend.securitycommon.constants.InternalAuthHeaders;
import com.bwg.channel.backend.securitycommon.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebFluxJwtAuthFilterTest {

    @Test
    void authenticatedRequestForwardsInternalAuthHeaders() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        WebFluxJwtAuthFilter filter = new WebFluxJwtAuthFilter(jwtUtil);
        String token = "access.jwt.token";
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "hong.gildong",
                null,
                List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("MANAGER"))
        );

        when(jwtUtil.validateAccessToken(token)).thenReturn(true);
        when(jwtUtil.getAuthenticationFromToken(token)).thenReturn(authentication);
        when(jwtUtil.getSessionId(token)).thenReturn("session-123");

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
            return reactor.core.publisher.Mono.empty();
        }).block();

        HttpHeaders headers = downstreamExchange.get().getRequest().getHeaders();
        assertThat(headers.getFirst(InternalAuthHeaders.USER)).isEqualTo("hong.gildong");
        assertThat(headers.getFirst(InternalAuthHeaders.ROLES)).isEqualTo("USER,MANAGER");
        assertThat(headers.getFirst(InternalAuthHeaders.SESSION_ID)).isEqualTo("session-123");
    }
}
