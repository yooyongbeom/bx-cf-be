package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

/** 공통코드 통합 등록/교체에 포함되는 상세코드 요청 모델 */
@Alias("CommonCodeReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCode", endpoints = {"create", "replace"})
public class CommonCodeReqDto {

    @ApiField(description = "공통코드", example = "Y", required = {"create", "replace"})
    private String code;

    @ApiField(description = "공통코드명", example = "사용", required = {"create", "replace"})
    private String codeNm;

    @ApiField(description = "공통코드 설명", optional = {"create", "replace"})
    private String codeDesc;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create", "replace"})
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create", "replace"})
    private String useYn;

    @ApiField(description = "유효 시작일", example = "2026-01-01", optional = {"create", "replace"})
    private LocalDate validFrom;

    @ApiField(description = "유효 종료일", example = "2026-12-31", optional = {"create", "replace"})
    private LocalDate validTo;

    @ApiField(description = "추가 데이터 JSON 문자열", example = "{}", optional = {"create", "replace"})
    private String extraData;

}
