package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청 DTO에서 선택적으로 사용하는 공통 검색 조건 모델.
 */
@Data
public class BaseSearchReqDto {

    @ApiField(description = "통합 검색어", example = "검색어", optional = {"list"})
    private String keyword;

    @ApiField(description = "검색 대상 구분", example = "productNm", optional = {"list"})
    private String searchType;

    @ApiField(description = "사용 여부 (Y/N)", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;

    @ApiField(description = "페이지 번호", example = "1", optional = {"list"})
    private Integer page;

    @ApiField(description = "페이지 크기", example = "20", optional = {"list"})
    private Integer size;

    @ApiField(description = "정렬 조건", example = "createdAt,desc", optional = {"list"})
    private String sort;

    public Integer getOffset() {
        if (page == null || size == null || page < 1 || size < 1) {
            return null;
        }
        return (page - 1) * size;
    }
}
