package com.bwg.channel.backend.authsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiDto(
    name = "Auth",
    endpoints = {"login", "erp-login"}
)
public class LoginDto {

    @ApiField(description = "사용자 ID", example = "hong.gildong",
              required = {"login", "erp-login"})
    private String usrId;

    @ApiField(description = "비밀번호", format = "password",
              required = {"login"},
              exclude = {"erp-login"})
    private String usrPwd;

    @ApiField(description = "사용자명", example = "홍길동", responseOnly = true)
    private String usrNm;

    @ApiField(description = "직위명", example = "대리", responseOnly = true)
    private String positDivName;

    @ApiField(description = "부서명", example = "채널개발팀", responseOnly = true)
    private String deptName;

    @ApiField(description = "액세스 토큰", responseOnly = true)
    private String accessToken;

    @ApiField(description = "액세스 토큰 만료 일시", responseOnly = true)
    private String accessTokenExpiresAt;

    @ApiField(description = "리프레시 토큰", responseOnly = true)
    private String refreshToken;

    @ApiField(description = "리프레시 토큰 만료 일시", responseOnly = true)
    private String refreshTokenExpiresAt;

    @ApiField(description = "권한 목록", responseOnly = true)
    private List<String> roles;
}
