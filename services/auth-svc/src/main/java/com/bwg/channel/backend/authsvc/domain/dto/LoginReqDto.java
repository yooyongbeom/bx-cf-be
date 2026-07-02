package com.bwg.channel.backend.authsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 요청 모델.
 *
 * <p>일반 로그인은 사용자 ID/PW를 DB 조회 조건으로 사용하고, ERP 로그인은 ERP 인증 전문 입력값으로 사용한다.</p>
 */
@Getter
@Setter
@ApiDto(type = ApiType.REQUEST, name = "Auth", endpoints = {"login", "erp-login"})
public class LoginReqDto {

    /** 일반 로그인 DB 조회와 ERP 인증 요청에 공통으로 쓰는 사용자 ID. */
    @ApiField(description = "사용자 ID", example = "hong.gildong", required = {"login", "erp-login"})
    private String usrId;

    /** 일반 로그인 DB 조회와 ERP 인증 요청에 사용하는 비밀번호. */
    @ApiField(description = "비밀번호", format = "password", required = {"login"})
    private String usrPwd;
}
