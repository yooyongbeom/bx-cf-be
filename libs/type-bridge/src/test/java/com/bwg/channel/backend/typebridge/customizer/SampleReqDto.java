package com.bwg.channel.backend.typebridge.customizer;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;

/** 신규 방식(요청/응답 분리) 테스트용 요청 DTO */
@ApiDto(type = ApiType.REQUEST, name = "Sample", endpoints = {"list"})
class SampleReqDto {

    @ApiField(description = "이름", optional = {"list"})
    private String name;
}
