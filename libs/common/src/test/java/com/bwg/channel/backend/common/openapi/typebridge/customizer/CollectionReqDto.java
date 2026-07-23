package com.bwg.channel.backend.common.openapi.typebridge.customizer;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;

import java.util.List;

/** 중첩 목록 요청 예제 생성을 검증하기 위한 최상위 요청 DTO. */
@ApiDto(type = ApiType.REQUEST, name = "Collection", endpoints = {"create"})
class CollectionReqDto {

    @ApiField(description = "그룹 코드", example = "USE_YN", required = {"create"})
    private String groupCd;

    @ApiField(description = "상세코드 목록", required = {"create"})
    private List<CollectionItemReqDto> codes;
}
