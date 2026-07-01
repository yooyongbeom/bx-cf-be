package com.bwg.channel.backend.authcore.filter;

import com.bwg.channel.backend.authcore.constants.AuthErrorCode;
import com.bwg.channel.backend.authcore.support.WebFluxAuthErrorResponseWriter;
import com.bwg.channel.backend.authcore.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class WebFluxJwtAuthFilter implements WebFilter {
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange);

        if (token != null) {
            try {
                jwtUtil.validateAccessToken(token);

                Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
                return chain.filter(exchange)
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
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, AuthErrorCode errorCode) {
        return WebFluxAuthErrorResponseWriter.write(exchange, HttpStatus.UNAUTHORIZED, errorCode);
    }
}
