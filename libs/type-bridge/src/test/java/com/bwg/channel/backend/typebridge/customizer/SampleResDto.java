package com.bwg.channel.backend.typebridge.customizer;

import com.bwg.channel.backend.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.typebridge.annotation.ApiField;
import com.bwg.channel.backend.typebridge.annotation.ApiType;

/** 신규 방식(요청/응답 분리) 테스트용 응답 DTO. list=요약, detail=전체로 필드/필수가 다르다. */
@ApiDto(type = ApiType.RESPONSE, name = "Sample", endpoints = {"list", "detail"})
class SampleResDto {

    @ApiField(description = "ID", required = {"list", "detail"})
    private Long id;

    @ApiField(description = "상세 설명", required = {"detail"})   // detail 응답에만 노출
    private String detailDesc;

    @ApiField(description = "값", required = {"detail"}, optional = {"list"}) // list=선택, detail=필수
    private Long value;
}
