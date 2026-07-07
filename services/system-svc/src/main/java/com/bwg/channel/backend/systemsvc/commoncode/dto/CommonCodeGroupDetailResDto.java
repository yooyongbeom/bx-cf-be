package com.bwg.channel.backend.systemsvc.commoncode.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.type.Alias;

/** 공통코드 그룹과 하위 공통코드 목록을 함께 제공하는 상세 응답 모델 */
@Alias("CommonCodeGroupDetailResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "CommonCodeGroupDetail", endpoints = {"detail"})
public class CommonCodeGroupDetailResDto {

    @ApiField(description = "공통코드 그룹 코드", example = "USE_YN", optional = {"detail"})
    private String groupCd;

    @ApiField(description = "공통코드 그룹명", example = "사용 여부", optional = {"detail"})
    private String groupNm;

    @ApiField(description = "공통코드 그룹 설명", optional = {"detail"})
    private String groupDesc;

    @ApiField(description = "시스템 코드 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String systemYn;

    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String useYn;

    @ApiField(description = "공통코드 목록", optional = {"detail"})
    private List<CommonCodeResDto> codes = new ArrayList<>();
}