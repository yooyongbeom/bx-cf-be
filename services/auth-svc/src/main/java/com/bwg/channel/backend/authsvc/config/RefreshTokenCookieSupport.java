package com.bwg.channel.backend.authsvc.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * refresh token을 HttpOnly 쿠키로 쓰고 요청 쿠키에서 다시 꺼내는 공통 유틸리티.
 */
@Component
public class RefreshTokenCookieSupport {

    private final RefreshTokenCookieProperties properties;

    public RefreshTokenCookieSupport(RefreshTokenCookieProperties properties) {
        this.properties = properties;
    }

    /**
     * 설정된 쿠키 정책을 적용해 refresh token 쿠키를 응답 헤더에 추가한다.
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(properties.getName(), refreshToken)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath())
                .maxAge(Duration.ofSeconds(properties.getMaxAgeSeconds()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 요청 쿠키 목록에서 refresh token 쿠키 값을 찾아 반환한다.
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (properties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
