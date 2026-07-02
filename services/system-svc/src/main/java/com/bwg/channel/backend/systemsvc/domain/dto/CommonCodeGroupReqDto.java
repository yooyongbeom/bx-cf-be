package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;

/** 공통코드 그룹 등록/수정 요청 모델 */
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCodeGroup", endpoints = {"create", "update"})
public class CommonCodeGroupReqDto {

    @ApiField(description = "공통코드 그룹 코드", example = "USE_YN", required = {"create"})
    private String groupCd;

    @ApiField(description = "공통코드 그룹명", example = "사용 여부", required = {"create", "update"})
    private String groupNm;

    @ApiField(description = "공통코드 그룹 설명", optional = {"create", "update"})
    private String groupDesc;

    @ApiField(description = "시스템 코드 여부", example = "N", allowableValues = {"Y", "N"}, optional = {"create", "update"})
    private String systemYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create", "update"})
    private String useYn;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create", "update"})
    private Integer sortSeq;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"create"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"update"})
    private String updatedBy;
}
