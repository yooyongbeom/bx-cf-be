package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 공통 검색 조건 모델.
 */
@Data
public class FilterReqDto {

    @ApiField(description = "통합 검색어", example = "검색어", optional = {"list"})
    private String keyword;

    @ApiField(description = "검색 대상 구분", example = "userNm", optional = {"list"})
    private String searchType;

    @ApiField(description = "사용 여부 (Y/N)", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;
}
