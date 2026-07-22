package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;
import org.apache.ibatis.type.Alias;

/** 공통코드 등록/수정 요청 모델 */
@Alias("CommonCodeReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCode", endpoints = {"create", "update"})
public class CommonCodeReqDto {

    // 경로 변수로 주입되는 값이므로 요청 스키마에 노출하지 않는다.
    @ApiField(description = "공통코드 그룹 코드", hidden = true)
    private String groupCd;

    @ApiField(description = "공통코드", example = "Y", required = {"create"})
    private String code;

    @ApiField(description = "공통코드명", example = "사용", required = {"create", "update"})
    private String codeNm;

    @ApiField(description = "공통코드 설명", optional = {"create", "update"})
    private String codeDesc;

    @ApiField(description = "상위 공통코드 ID", optional = {"create", "update"})
    private Long parentCodeId;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create", "update"})
    private Integer sortSeq;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create", "update"})
    private String useYn;

    @ApiField(description = "유효 시작일", example = "2026-01-01", optional = {"create", "update"})
    private LocalDate validFrom;

    @ApiField(description = "유효 종료일", example = "2026-12-31", optional = {"create", "update"})
    private LocalDate validTo;

    @ApiField(description = "추가 데이터 JSON 문자열", example = "{}", optional = {"create", "update"})
    private String extraData;

}
