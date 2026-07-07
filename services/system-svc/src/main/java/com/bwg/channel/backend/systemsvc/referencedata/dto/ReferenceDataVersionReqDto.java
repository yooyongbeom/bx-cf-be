package com.bwg.channel.backend.systemsvc.referencedata.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/** 기준정보 최신 버전 조회 요청 모델 */
@Alias("ReferenceDataVersionReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "ReferenceDataVersion", endpoints = {"latest"})
public class ReferenceDataVersionReqDto {

    @ApiField(description = "기준정보 유형", example = "ALL", required = {"latest"})
    private String refType;
}
