package com.bwg.channel.backend.authsvc.authentication.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * 로그인과 토큰 재발급 응답 모델.
 *
 * <p>access token은 응답 본문으로 전달하고, refresh token은 컨트롤러에서 HttpOnly 쿠키로만 전달한다.</p>
 */
@Alias("LoginResDto")
@Getter
@Setter
@ApiDto(type = ApiType.RESPONSE, name = "Auth", endpoints = {"login", "erp-login", "refresh-token"})
public class LoginResDto {

    /** DB 사용자 조회와 JWT subject에 사용하는 사용자 ID. */
    @ApiField(description = "사용자 ID", example = "hong.gildong",
              required = {"login", "erp-login", "refresh-token"})
    private String usrId;

    /** 로그인 성공 후 화면 표시나 로그에 사용할 사용자명. */
    @ApiField(description = "사용자명", example = "홍길동",
              optional = {"login", "erp-login", "refresh-token"})
    private String usrNm;

    /** ERP/JPA/MyBatis 조회 결과에서 전달되는 직위명. */
    @ApiField(description = "직위명", example = "대리",
              optional = {"login", "erp-login", "refresh-token"})
    private String positDivName;

    /** ERP/JPA/MyBatis 조회 결과에서 전달되는 부서명. */
    @ApiField(description = "부서명", example = "채널개발팀",
              optional = {"login", "erp-login", "refresh-token"})
    private String deptName;

    /** Gateway 인증 필터가 검증하고 내부 인증 헤더 생성에 사용할 access token. */
    @ApiField(description = "액세스 토큰",
              required = {"login", "erp-login", "refresh-token"})
    private String accessToken;

    /** 클라이언트가 access token 교체 시점을 판단할 수 있는 만료 일시. */
    @ApiField(description = "액세스 토큰 만료 일시",
              required = {"login", "erp-login", "refresh-token"})
    private String accessTokenExpiresAt;

    /** access token claim과 내부 서비스 권한 판단에 사용할 권한 목록. */
    @ApiField(description = "권한 목록",
              optional = {"login", "erp-login", "refresh-token"})
    private List<String> roles;

    /** DB 저장과 쿠키 전달에만 사용하는 refresh token, JSON 응답과 OpenAPI 스키마에서는 제외. */
    @JsonIgnore
    private String refreshToken;

    /** Redis 세션 TTL 계산과 DB 저장에만 사용하는 refresh token 만료 일시. */
    @JsonIgnore
    private String refreshTokenExpiresAt;
}
