package com.bwg.channel.backend.authsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 로그인/토큰 재발급 응답 모델 겸 인증 처리 캐리어.
 * <p>
 * 응답 본문/스키마에 노출되는 필드는 {@code @ApiField}로 지정하고, refresh token은
 * 서비스→컨트롤러로만 전달되어 HttpOnly 쿠키로 내려가야 하므로 {@code @ApiField} 없이 {@code @JsonIgnore}로 둔다.
 * (그래서 OpenAPI 스키마에도, 응답 JSON에도 나타나지 않는다.)
 */
@Getter
@Setter
@ApiDto(type = ApiType.RESPONSE, name = "Auth", endpoints = {"login", "erp-login", "refresh-token"})
public class LoginResDto {

    @ApiField(description = "사용자 ID", example = "hong.gildong",
              required = {"login", "erp-login", "refresh-token"})
    private String usrId;

    @ApiField(description = "사용자명", example = "홍길동",
              optional = {"login", "erp-login", "refresh-token"})
    private String usrNm;

    @ApiField(description = "직위명", example = "대리",
              optional = {"login", "erp-login", "refresh-token"})
    private String positDivName;

    @ApiField(description = "부서명", example = "채널개발팀",
              optional = {"login", "erp-login", "refresh-token"})
    private String deptName;

    @ApiField(description = "액세스 토큰",
              required = {"login", "erp-login", "refresh-token"})
    private String accessToken;

    @ApiField(description = "액세스 토큰 만료 일시",
              required = {"login", "erp-login", "refresh-token"})
    private String accessTokenExpiresAt;

    @ApiField(description = "권한 목록",
              optional = {"login", "erp-login", "refresh-token"})
    private List<String> roles;

    // ── 내부 캐리어 필드 (응답 스키마/본문 비노출) ─────────────────────────────
    // refresh token은 HttpOnly 쿠키로만 내려가므로 @ApiField 없이 @JsonIgnore로 숨긴다.

    @JsonIgnore
    private String refreshToken;

    @JsonIgnore
    private String refreshTokenExpiresAt;
}
