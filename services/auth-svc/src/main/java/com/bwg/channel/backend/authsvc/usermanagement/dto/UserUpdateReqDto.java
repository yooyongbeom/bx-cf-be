package com.bwg.channel.backend.authsvc.usermanagement.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 사용자 기본정보 수정 요청 모델.
 */
@Alias("UserUpdateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "User", endpoints = {"update"})
public class UserUpdateReqDto {

    /** 수정 후 계정에 표시할 사용자명. */
    @ApiField(description = "사용자명", example = "홍길동", required = {"update"})
    private String usrNm;

    /** 수정 가능한 사용자 조직 정보인 직위명. */
    @ApiField(description = "직위명", example = "차장", optional = {"update"})
    private String positDivName;

    /** 수정 가능한 사용자 조직 정보인 부서명. */
    @ApiField(description = "부서명", example = "채널개발팀", optional = {"update"})
    private String deptName;
}
