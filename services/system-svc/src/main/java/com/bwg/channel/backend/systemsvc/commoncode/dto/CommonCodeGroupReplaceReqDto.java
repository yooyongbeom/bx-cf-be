package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/** 공통코드 그룹과 하위 코드 통합 교체 시 수정할 그룹 정보 */
@Alias("CommonCodeGroupReplaceReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "CommonCodeGroupReplace", endpoints = {"replace"})
public class CommonCodeGroupReplaceReqDto {

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
}
