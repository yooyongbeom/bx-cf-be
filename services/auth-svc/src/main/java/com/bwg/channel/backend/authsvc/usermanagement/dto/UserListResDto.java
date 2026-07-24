package com.bwg.channel.backend.authsvc.usermanagement.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/**
 * 사용자 목록 응답 모델.
 */
@Alias("UserListResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "User", endpoints = {"list"})
public class UserListResDto {

    /** 목록에서 사용자를 식별하는 사용자 ID. */
    @ApiField(description = "사용자 ID", example = "hong.gildong", optional = {"list"})
    private String usrId;

    /** 목록 화면에 표시할 사용자명. */
    @ApiField(description = "사용자명", example = "홍길동", optional = {"list"})
    private String usrNm;

    /** 목록 화면에 표시할 직위명. */
    @ApiField(description = "직위명", example = "과장", optional = {"list"})
    private String positDivName;

    /** 목록 화면에 표시할 부서명. */
    @ApiField(description = "부서명", example = "채널개발팀", optional = {"list"})
    private String deptName;

    /** 사용자 레코드를 최초 등록한 인증된 사용자 ID. */
    @ApiField(description = "생성자 ID", example = "admin", optional = {"list"})
    private String createdBy;

    /** 사용자 레코드가 최초 생성된 일시. */
    @ApiField(description = "생성 일시", example = "2026-07-23T09:00:00+09:00", optional = {"list"})
    private OffsetDateTime createdAt;

    /** 사용자 레코드를 마지막으로 수정한 인증된 사용자 ID. */
    @ApiField(description = "수정자 ID", example = "admin", optional = {"list"})
    private String updatedBy;

    /** 사용자 레코드가 마지막으로 수정된 일시. */
    @ApiField(description = "수정 일시", example = "2026-07-23T10:00:00+09:00", optional = {"list"})
    private OffsetDateTime updatedAt;
}
