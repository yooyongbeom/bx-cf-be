package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 정렬 조건 모델
 * <p>
 * 정렬 문자열은 서비스 쿼리 구현에서 해석하며, 공통 모델은 전달 형식만 표준화한다.
 */
@Data
public class SortReqDto {

    @ApiField(description = "정렬 조건", example = "createdAt,desc", optional = {"list"})
    private String sort;
}
