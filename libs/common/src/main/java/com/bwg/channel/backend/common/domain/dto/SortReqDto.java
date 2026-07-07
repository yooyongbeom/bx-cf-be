package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 정렬 조건 모델.
 */
@Data
public class SortReqDto {

    @ApiField(description = "정렬 조건", example = "createdAt,desc", optional = {"list"})
    private String sort;
}
