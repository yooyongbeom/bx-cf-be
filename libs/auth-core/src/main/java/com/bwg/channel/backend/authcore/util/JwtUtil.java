package com.bwg.channel.backend.authcore.util;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${spring.jwt.secret}")
    public String secretKey;

    @Value("${spring.jwt.access-token-validity-ms}")
    private long accessTokenValidityMillis;

    @Value("${spring.jwt.refresh-token-validity-ms}")
    private long refreshTokenValidityMillis;
    private Key signingKey;

    @PostConstruct
    protected void init() {
        // HS256 알고리즘용 Key 객체 생성
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성
    public String createAccessToken(String userName, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put("roles", roles);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String userName) {
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Jwt 토큰으로 인증
    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = parseToken(token);
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Only access tokens can be used for authentication");
        }

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) {
            throw new JwtException("Access token does not contain roles");
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(getSubject(token), null, authorities);
    }

    // Jwt 토큰으로 DB 조회 인증 나중에 auth-svc에 구현하자
//    public Authentication getAuthenticationFromDb(String token) {
//        UserDetails user = userDetailsService.loadUserByUsername(getSubject(token));
//        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//    }

    // WebFlux용
    public Mono<Authentication> getWebFluxAuthentication(String token) {
        return validateAccessToken(token) ? Mono.just(getAuthenticationFromToken(token)) : Mono.empty();
    }

    // Jwt 토큰에서 구별 정보 추출
    public String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Jwt 토큰의 유효성 + 만료일자 확인 (예외를 그대로 던짐)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateAccessToken(String token) {
        Claims claims = parseToken(token);
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid access token type");
        }
        return true;
    }

    public boolean validateRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid refresh token type");
        }
        return true;
    }

    // Jwt 토큰에서 만료일자 추출
    public Date getExpirationDateFromToken(String token) {
        return parseToken(token).getExpiration();
    }
}
