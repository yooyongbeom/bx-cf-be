package com.bwg.channel.backend.authsvc.user.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.OffsetDateTime;

/**
 * 사용자 상세 응답 모델.
 */
@Alias("UserDetailResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "User", endpoints = {"detail"})
public class UserDetailResDto {

    /** 상세 조회한 사용자를 식별하는 사용자 ID. */
    @ApiField(description = "사용자 ID", example = "hong.gildong", optional = {"detail"})
    private String usrId;

    /** 상세 화면에 표시할 사용자명. */
    @ApiField(description = "사용자명", example = "홍길동", optional = {"detail"})
    private String usrNm;

    /** 상세 화면에 표시할 직위명. */
    @ApiField(description = "직위명", example = "과장", optional = {"detail"})
    private String positDivName;

    /** 상세 화면에 표시할 부서명. */
    @ApiField(description = "부서명", example = "채널개발팀", optional = {"detail"})
    private String deptName;

    /** 사용자 레코드를 최초 등록한 인증된 사용자 ID. */
    @ApiField(description = "생성자 ID", example = "admin", optional = {"detail"})
    private String createdBy;

    /** 사용자 레코드가 최초 생성된 일시. */
    @ApiField(description = "생성 일시", example = "2026-07-23T09:00:00+09:00", optional = {"detail"})
    private OffsetDateTime createdAt;

    /** 사용자 레코드를 마지막으로 수정한 인증된 사용자 ID. */
    @ApiField(description = "수정자 ID", example = "admin", optional = {"detail"})
    private String updatedBy;

    /** 사용자 레코드가 마지막으로 수정된 일시. */
    @ApiField(description = "수정 일시", example = "2026-07-23T10:00:00+09:00", optional = {"detail"})
    private OffsetDateTime updatedAt;
}
