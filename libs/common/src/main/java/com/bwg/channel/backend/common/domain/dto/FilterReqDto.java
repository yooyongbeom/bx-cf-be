package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 공통 검색 조건 모델
 * <p>
 * 서비스별 상세 검색 조건은 각 서비스 DTO의 {@code data} 또는 별도 필드에서 확장하고,
 * 이 모델은 공통 목록 화면에서 반복되는 단순 검색 조건만 담는다.
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
