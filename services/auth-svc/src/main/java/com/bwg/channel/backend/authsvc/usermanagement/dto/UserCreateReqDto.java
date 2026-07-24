package com.bwg.channel.backend.authsvc.usermanagement.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * 사용자 등록 요청 모델.
 */
@Alias("UserCreateReqDto")
@Getter
@Setter
@ApiDto(type = ApiType.REQUEST, name = "User", endpoints = {"create"})
public class UserCreateReqDto {

    /** 등록할 사용자를 식별하는 사용자 ID. */
    @ApiField(description = "사용자 ID", example = "hong.gildong", required = {"create"})
    private String usrId;

    /** 사용자 계정에 표시할 사용자명. */
    @ApiField(description = "사용자명", example = "홍길동", required = {"create"})
    private String usrNm;

    /** 사용자 조직 정보로 함께 등록할 직위명. */
    @ApiField(description = "직위명", example = "과장", optional = {"create"})
    private String positDivName;

    /** 사용자 조직 정보로 함께 등록할 부서명. */
    @ApiField(description = "부서명", example = "채널개발팀", optional = {"create"})
    private String deptName;

    /** 등록 시에만 입력받아 암호화 대상으로 전달하는 비밀번호. */
    @ApiField(description = "비밀번호", format = "password", required = {"create"})
    private String usrPwd;
}
