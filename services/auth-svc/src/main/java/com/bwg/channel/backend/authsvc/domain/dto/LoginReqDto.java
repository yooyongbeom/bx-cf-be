package com.bwg.channel.backend.authsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 모델. (일반 로그인은 ID/비밀번호, ERP 로그인은 ID만 사용)
 */
@Getter
@Setter
@ApiDto(type = ApiType.REQUEST, name = "Auth", endpoints = {"login", "erp-login"})
public class LoginReqDto {

    @ApiField(description = "사용자 ID", example = "hong.gildong", required = {"login", "erp-login"})
    private String usrId;

    @ApiField(description = "비밀번호", format = "password", required = {"login"})
    private String usrPwd;
}
