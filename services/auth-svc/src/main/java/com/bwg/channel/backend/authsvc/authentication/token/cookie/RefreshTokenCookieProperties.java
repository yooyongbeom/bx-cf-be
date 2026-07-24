package com.bwg.channel.backend.authsvc.authentication.token.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * {@code app.auth.refresh-cookie.*} 설정을 refresh token 쿠키 옵션으로 바인딩하는 프로퍼티.
 */
@Component
@ConfigurationProperties(prefix = "app.auth.refresh-cookie")
public class RefreshTokenCookieProperties {

    /** 브라우저에 저장되는 refresh token 쿠키 이름. */
    private String name = "refreshToken";

    /** refresh token 쿠키가 전송될 인증 API 경로 범위. */
    private String path = "/channel/backend/api/v1/auth";

    /** 크로스 사이트 요청에서 refresh token 쿠키 전송 범위를 제한하는 SameSite 정책. */
    private String sameSite = "Lax";

    /** HTTPS 요청에서만 refresh token 쿠키를 전송할지 여부. */
    private boolean secure = true;

    /** refresh token 쿠키의 브라우저 보관 시간. */
    private long maxAgeSeconds = 604800;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public long getMaxAgeSeconds() {
        return maxAgeSeconds;
    }

    public void setMaxAgeSeconds(long maxAgeSeconds) {
        this.maxAgeSeconds = maxAgeSeconds;
    }
}
