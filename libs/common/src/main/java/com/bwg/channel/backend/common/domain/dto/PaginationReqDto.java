package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 요청에서 사용하는 페이징 조건 모델
 * <p>
 * page는 1부터 시작하는 화면 기준 번호이고, {@link #getOffset()}에서 DB 조회용 0-base offset으로 변환한다.
 */
@Data
public class PaginationReqDto {

    @ApiField(description = "페이지 번호", example = "1", optional = {"list"})
    private Integer page;

    @ApiField(description = "페이지 크기", example = "20", optional = {"list"})
    private Integer size;

    /** page/size가 유효할 때만 MyBatis/JPA 조회에 사용할 offset을 계산한다. */
    public Integer getOffset() {
        if (page == null || size == null || page < 1 || size < 1) {
            return null;
        }
        return (page - 1) * size;
    }
}
