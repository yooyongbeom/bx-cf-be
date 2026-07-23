package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.List;

/** 공통코드 그룹 정보와 하위 상세코드 전체를 교체하기 위한 요청 모델 */
@Alias("CommonCodeReplaceReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCodeReplace", endpoints = {"replace"})
public class CommonCodeReplaceReqDto {

    @ApiField(description = "공통코드 그룹명", example = "사용 여부", required = {"replace"})
    private String groupNm;

    @ApiField(description = "공통코드 그룹 설명", optional = {"replace"})
    private String groupDesc;

    @ApiField(description = "시스템 코드 여부", example = "N", allowableValues = {"Y", "N"}, optional = {"replace"})
    private String systemYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"replace"})
    private String useYn;

    @ApiField(description = "정렬 순서", example = "1", optional = {"replace"})
    private Integer sortSeq;

    @ApiField(description = "교체할 상세코드 전체 목록", required = {"replace"})
    private List<CommonCodeReqDto> codes;
}
