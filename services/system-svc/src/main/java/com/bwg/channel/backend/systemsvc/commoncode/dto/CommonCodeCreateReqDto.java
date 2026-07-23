package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

/** 공통코드 그룹과 하위 상세코드를 함께 등록하는 요청 모델 */
@Alias("CommonCodeCreateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCodeCreate", endpoints = {"create"})
public class CommonCodeCreateReqDto {

    @ApiField(description = "공통코드 그룹 코드", example = "USE_YN", required = {"create"})
    private String groupCd;

    @ApiField(description = "공통코드 그룹명", example = "사용 여부", required = {"create"})
    private String groupNm;

    @ApiField(description = "공통코드 그룹 설명", optional = {"create"})
    private String groupDesc;

    @ApiField(description = "시스템 코드 여부", example = "N", allowableValues = {"Y", "N"}, optional = {"create"})
    private String systemYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create"})
    private String useYn;

    @ApiField(description = "정렬 순서", example = "1", optional = {"create"})
    private Integer sortSeq;

    @ApiField(description = "등록할 상세코드 목록(빈 배열이면 그룹만 등록)", required = {"create"})
    private List<CommonCodeReqDto> codes;
}
