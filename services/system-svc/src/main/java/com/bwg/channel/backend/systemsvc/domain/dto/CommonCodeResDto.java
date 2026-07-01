package com.bwg.channel.backend.systemsvc.domain.dto;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;
import lombok.Data;

import java.time.LocalDate;

/** 공통코드 목록 응답 모델 */
@Data
@ApiDto(type = ApiType.RESPONSE, name = "CommonCode", endpoints = {"list"})
public class CommonCodeResDto {

    @ApiField(description = "공통코드 ID", optional = {"list"})
    private Long codeId;

    @ApiField(description = "공통코드 그룹 ID", optional = {"list"})
    private Long groupId;

    @ApiField(description = "공통코드 그룹 코드", optional = {"list"})
    private String groupCd;

    @ApiField(description = "공통코드", example = "Y", optional = {"list"})
    private String code;

    @ApiField(description = "공통코드명", example = "사용", optional = {"list"})
    private String codeNm;

    @ApiField(description = "공통코드 설명", optional = {"list"})
    private String codeDesc;

    @ApiField(description = "상위 공통코드 ID", optional = {"list"})
    private Long parentCodeId;

    @ApiField(description = "정렬 순서", example = "1", optional = {"list"})
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;

    @ApiField(description = "유효 시작일", example = "2026-01-01", optional = {"list"})
    private LocalDate validFrom;

    @ApiField(description = "유효 종료일", example = "2026-12-31", optional = {"list"})
    private LocalDate validTo;

    @ApiField(description = "추가 데이터(JSON 문자열)", example = "{}", optional = {"list"})
    private String extraData;

    @ApiField(description = "생성자 ID", example = "admin", optional = {"list"})
    private String createdBy;

    @ApiField(description = "수정자 ID", example = "admin", optional = {"list"})
    private String updatedBy;
}
