package com.bwg.channel.backend.authsvc.authentication.token.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * refresh token을 응답 본문 대신 HttpOnly 쿠키로 전달하고 요청 쿠키에서 다시 읽는 지원 객체.
 */
@Component
public class RefreshTokenCookieSupport {

    /** application.yml에서 바인딩된 refresh token 쿠키 옵션. */
    private final RefreshTokenCookieProperties properties;

    public RefreshTokenCookieSupport(RefreshTokenCookieProperties properties) {
        this.properties = properties;
    }

    /**
     * 발급된 refresh token을 설정된 이름/경로/보안 옵션의 HttpOnly 응답 쿠키로 변환한다.
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        // refresh token 노출을 줄이기 위한 HttpOnly Set-Cookie 값 구성
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
     * 로그아웃 시 브라우저에 남아 있는 refresh token 쿠키를 즉시 만료시킨다.
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        // 기존 쿠키와 같은 name/path/sameSite/secure 값에 Max-Age=0 적용
        ResponseCookie cookie = ResponseCookie.from(properties.getName(), "")
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath())
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 브라우저가 자동 전송한 쿠키 목록에서 refresh token 값을 찾는다.
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // 설정된 쿠키 이름과 일치하는 값만 refresh token으로 사용
        for (var cookie : request.getCookies()) {
            if (properties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
