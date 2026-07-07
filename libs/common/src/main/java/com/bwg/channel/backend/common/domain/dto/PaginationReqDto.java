package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 페이징 조건 모델.
 */
@Data
public class PaginationReqDto {

    @ApiField(description = "페이지 번호", example = "1", optional = {"list"})
    private Integer page;

    @ApiField(description = "페이지 크기", example = "20", optional = {"list"})
    private Integer size;

    public Integer getOffset() {
        if (page == null || size == null || page < 1 || size < 1) {
            return null;
        }
        return (page - 1) * size;
    }
}
