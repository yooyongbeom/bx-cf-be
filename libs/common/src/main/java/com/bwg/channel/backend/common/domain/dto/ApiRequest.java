package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 요청 본문을 관심사별 블록으로 분리하기 위한 공통 요청 모델.
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
