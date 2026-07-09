package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import lombok.Data;

/**
 * 목록 조회 응답에서 사용하는 페이지 결과 정보 모델
 * <p>
 * {@link ApiResponse#success(Object, PaginationResDto)}를 통해 payload와 함께 내려가며,
 * 클라이언트가 다음/이전 페이지 여부를 계산할 때 사용하는 메타데이터를 담는다.
 */
@Data
public class PaginationResDto {

    @ApiField(description = "페이지 번호", example = "1", optional = {"list"})
    private Integer page;

    @ApiField(description = "페이지 크기", example = "20", optional = {"list"})
    private Integer size;

    @ApiField(description = "전체 건수", example = "100", optional = {"list"})
    private Long totalCount;

    @ApiField(description = "전체 페이지 수", example = "5", optional = {"list"})
    private Integer totalPages;
}
