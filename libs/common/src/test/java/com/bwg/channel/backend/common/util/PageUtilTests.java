package com.bwg.channel.backend.common.util;

import com.bwg.channel.backend.common.domain.dto.PaginationReqDto;
import com.bwg.channel.backend.common.domain.dto.PaginationResDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageUtilTests {

    @Test
    void singlePageUsesTheEntireCollectionAsPaginationMetadata() {
        PaginationResDto pagination = PageUtil.singlePage(List.of("A", "B", "C"));

        assertThat(pagination.getPage()).isEqualTo(1);
        assertThat(pagination.getSize()).isEqualTo(3);
        assertThat(pagination.getTotalCount()).isEqualTo(3L);
        assertThat(pagination.getTotalPages()).isEqualTo(1);
    }

    @Test
    void singlePageTreatsNullAsAnEmptyCollection() {
        PaginationResDto pagination = PageUtil.singlePage(null);

        assertThat(pagination.getPage()).isEqualTo(1);
        assertThat(pagination.getSize()).isZero();
        assertThat(pagination.getTotalCount()).isZero();
        assertThat(pagination.getTotalPages()).isZero();
    }

    @Test
    void ofCalculatesTotalPagesFromTheActualTotalCount() {
        PaginationReqDto request = new PaginationReqDto();
        request.setPage(2);
        request.setSize(20);

        PaginationResDto pagination = PageUtil.of(request, 41L);

        assertThat(pagination.getPage()).isEqualTo(2);
        assertThat(pagination.getSize()).isEqualTo(20);
        assertThat(pagination.getTotalCount()).isEqualTo(41L);
        assertThat(pagination.getTotalPages()).isEqualTo(3);
    }

    @Test
    void ofReturnsNullWithoutAPaginationRequest() {
        assertThat(PageUtil.of(null, 41L)).isNull();
    }
}
