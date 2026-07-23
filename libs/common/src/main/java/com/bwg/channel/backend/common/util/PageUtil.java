package com.bwg.channel.backend.common.util;

import com.bwg.channel.backend.common.domain.dto.PaginationReqDto;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;

import java.util.Collection;

/**
 * 전체 목록과 실제 페이징 조회 결과의 응답 페이지 메타데이터를 생성하는 공통 유틸리티.
 */
public final class PageUtil {

    private PageUtil() {
    }

    /**
     * 전체 조회 결과를 한 페이지로 표현하는 페이지 메타데이터를 생성한다.
     *
     * @param result 전체 조회 결과, {@code null}이면 빈 목록으로 처리
     * @return 첫 페이지에 전체 결과가 포함된 페이지 메타데이터
     */
    public static PaginationResDto singlePage(Collection<?> result) {
        int totalCount = result == null ? 0 : result.size();
        PaginationResDto pagination = new PaginationResDto();
        pagination.setPage(1);
        pagination.setSize(totalCount);
        pagination.setTotalCount((long) totalCount);
        pagination.setTotalPages(totalCount == 0 ? 0 : 1);
        return pagination;
    }

    /**
     * 요청 페이지와 저장소가 계산한 전체 건수로 응답 페이지 메타데이터를 생성한다.
     *
     * @param requestPagination 요청 페이지 정보
     * @param totalCount 검색 조건에 해당하는 전체 건수
     * @return 계산된 페이지 메타데이터, 페이지 요청이 없으면 {@code null}
     */
    public static PaginationResDto of(PaginationReqDto requestPagination, long totalCount) {
        if (requestPagination == null) {
            return null;
        }

        PaginationResDto pagination = new PaginationResDto();
        pagination.setPage(requestPagination.getPage());
        pagination.setSize(requestPagination.getSize());
        pagination.setTotalCount(totalCount);
        pagination.setTotalPages(calculateTotalPages(totalCount, requestPagination.getSize()));
        return pagination;
    }

    /**
     * 전체 건수와 페이지 크기로 전체 페이지 수를 올림 계산한다.
     *
     * @param totalCount 전체 건수
     * @param size 페이지당 건수
     * @return 전체 페이지 수, 페이지 크기가 유효하지 않으면 {@code null}
     */
    private static Integer calculateTotalPages(long totalCount, Integer size) {
        if (size == null || size < 1) {
            return null;
        }
        return (int) Math.ceil((double) totalCount / size);
    }
}
