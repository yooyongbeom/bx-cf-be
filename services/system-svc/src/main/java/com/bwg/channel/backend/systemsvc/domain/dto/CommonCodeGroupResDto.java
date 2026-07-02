package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;

/** 공통코드 그룹 목록 응답 모델 */
@Data
@ApiDto(type = ApiType.RESPONSE, name = "CommonCodeGroup", endpoints = {"list"})
public class CommonCodeGroupResDto {

    @ApiField(description = "공통코드 그룹 ID", optional = {"list"})
    private Long groupId;

    @ApiField(description = "공통코드 그룹 코드", example = "USE_YN", optional = {"list"})
    private String groupCd;

    @ApiField(description = "공통코드 그룹명", example = "사용 여부", optional = {"list"})
    private String groupNm;

    @ApiField(description = "공통코드 그룹 설명", optional = {"list"})
    private String groupDesc;

    @ApiField(description = "시스템 코드 여부", example = "N", allowableValues = {"Y", "N"}, optional = {"list"})
    private String systemYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list"})
    private Integer sortSeq;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list"})
    private String updatedBy;
}
