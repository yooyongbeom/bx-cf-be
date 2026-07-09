package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 요청 본문을 분리하기 위한 공통 요청 모델.
 * <p>
 * 목록 조회는 {@code pagination/filter/sort}를, 생성·수정 계열은 {@code data}를 중심으로 사용한다.
 * TypeBridge는 이 구조를 기준으로 엔드포인트별 Request 스키마를 생성한다.
 */
@Alias("ApiRequest")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequest<T> {

    @ApiField(description = "페이징 정보", optional = {"list"})
    private PaginationReqDto pagination;

    @ApiField(description = "검색 조건", optional = {"list"})
    private FilterReqDto filter;

    @ApiField(description = "정렬 조건", optional = {"list"})
    private SortReqDto sort;

    @ApiField(description = "요청 데이터", optional = {"list", "create", "update"})
    private T data;
}
