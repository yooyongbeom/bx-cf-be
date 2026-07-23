package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;

/** 중첩 목록 요청 예제 생성을 검증하기 위한 상세 항목 DTO. */
@ApiDto(type = ApiType.REQUEST, name = "CollectionItem", endpoints = {"create"})
class CollectionItemReqDto {

    @ApiField(description = "상세코드", example = "Y", required = {"create"})
    private String code;
}
