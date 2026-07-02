package com.bwg.channel.backend.securitycommon.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
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
    /** access/refresh token 구분에 사용하는 JWT claim 이름. */
    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    /** Redis 세션 컨텍스트 key와 연결되는 JWT claim 이름. */
    public static final String SESSION_ID_CLAIM = "sessionId";

    /** Gateway 인증 필터가 허용하는 access token 타입 값. */
    private static final String ACCESS_TOKEN_TYPE = "access";

    /** 토큰 재발급 API에서만 허용하는 refresh token 타입 값. */
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    /** application.yml의 spring.jwt.secret 값, JWT 서명 키 원문. */
    @Value("${spring.jwt.secret}")
    public String secretKey;

    /** access token 만료 시간, 클라이언트 요청 인증 유효 시간을 결정. */
    @Value("${spring.jwt.access-token-validity-ms}")
    private long accessTokenValidityMillis;

    /** refresh token 만료 시간, DB 저장 refresh token과 Redis 세션 TTL 기준. */
    @Value("${spring.jwt.refresh-token-validity-ms}")
    private long refreshTokenValidityMillis;

    /** secretKey를 UTF-8 bytes로 변환해 만든 HS256 서명 키. */
    private Key signingKey;

    @PostConstruct
    protected void init() {
        // UTF-8 secret 기반 HS256 서명 키 생성
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 기존 호출부 호환용 access token 발급
    public String createAccessToken(String userName, List<String> roles) {
        return createAccessToken(userName, roles, null);
    }

    public String createAccessToken(String userName, List<String> roles, String sessionId) {
        // Gateway와 내부 서비스가 사용할 사용자/권한/sessionId claim 구성
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put("roles", roles);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        if (sessionId != null && !sessionId.isBlank()) {
            claims.put(SESSION_ID_CLAIM, sessionId);
        }

        // access token 유효기간 기준 JWT 서명
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String userName) {
        // DB 저장과 토큰 재발급 검증에 사용할 refresh token claim 구성
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);

        // refresh token 유효기간 기준 JWT 서명
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthenticationFromToken(String token) {
        // 검증된 access token claim으로 Spring Security 인증 객체 구성
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

    // JWT subject 기반 DB 재조회 인증이 필요해질 때 auth-svc에서 확장할 후보
//    public Authentication getAuthenticationFromDb(String token) {
//        UserDetails user = userDetailsService.loadUserByUsername(getSubject(token));
//        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//    }

    public Mono<Authentication> getWebFluxAuthentication(String token) {
        // WebFlux 필터 체인에서 사용할 비동기 인증 객체 변환
        return validateAccessToken(token) ? Mono.just(getAuthenticationFromToken(token)) : Mono.empty();
    }

    public String getSubject(String token) {
        // JWT subject에서 사용자 식별자 추출
        return parseToken(token).getSubject();
    }

    public String getSessionId(String token) {
        // access token claim에서 Redis 세션 식별자 추출
        return parseToken(token).get(SESSION_ID_CLAIM, String.class);
    }

    public Claims parseToken(String token) throws JwtException {
        // 서명 검증이 완료된 JWT claim 파싱
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        // JWT 서명과 만료 상태 검증, 실패 사유는 호출부에서 처리할 수 있도록 예외 전파
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
        // access token 타입 claim 검증
        Claims claims = parseToken(token);
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid access token type");
        }
        return true;
    }

    public boolean validateRefreshToken(String token) {
        // refresh token 타입 claim 검증
        Claims claims = parseToken(token);
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new JwtException("Invalid refresh token type");
        }
        return true;
    }

    public Date getExpirationDateFromToken(String token) {
        // JWT 만료 시각 추출 후 응답 DTO와 Redis TTL 계산에 활용
        return parseToken(token).getExpiration();
    }
}
