package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CommonDtoContractTests {

    @Test
    void apiRequestDefinesSeparatedRequestBlocks() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.ApiRequest");

        assertApiField(dtoType, "pagination");
        assertApiField(dtoType, "filter");
        assertApiField(dtoType, "sort");
        assertApiField(dtoType, "data");
    }

    @Test
    void paginationRequestDtoDefinesOnlyRequestPagingFields() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.PaginationReqDto");

        assertApiField(dtoType, "page");
        assertApiField(dtoType, "size");
        assertThat(dtoType.getDeclaredFields())
                .extracting("name")
                .doesNotContain("totalCount", "totalPages");
    }

    @Test
    void filterAndSortRequestDtosDefineCommonBlocks() throws Exception {
        Class<?> filterType = Class.forName("com.bwg.channel.backend.common.domain.dto.FilterReqDto");
        Class<?> sortType = Class.forName("com.bwg.channel.backend.common.domain.dto.SortReqDto");

        assertApiField(filterType, "keyword");
        assertApiField(filterType, "searchType");
        assertApiField(filterType, "useYn");
        assertApiField(sortType, "sort");
    }

    @Test
    void paginationResponseDtoDefinesPagingResultFields() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.PaginationResDto");

        assertApiField(dtoType, "page");
        assertApiField(dtoType, "size");
        assertApiField(dtoType, "totalCount");
        assertApiField(dtoType, "totalPages");
    }

    @Test
    void apiResponseCanCarryListMetadata() throws Exception {
        Class<?> paginationType = Class.forName("com.bwg.channel.backend.common.domain.dto.PaginationResDto");
        Object pagination = paginationType.getDeclaredConstructor().newInstance();
        paginationType.getMethod("setPage", Integer.class).invoke(pagination, 1);
        paginationType.getMethod("setSize", Integer.class).invoke(pagination, 20);
        paginationType.getMethod("setTotalCount", Long.class).invoke(pagination, 100L);
        paginationType.getMethod("setTotalPages", Integer.class).invoke(pagination, 5);

        Object response = ApiResponse.class
                .getMethod("success", Object.class, paginationType)
                .invoke(null, "payload", pagination);

        assertApiField(ApiResponse.class, "pagination");
        assertThat(Arrays.stream(ApiResponse.class.getDeclaredFields()))
                .extracting("name")
                .doesNotContain("sort");
        assertThat(ApiResponse.class.getMethod("getPagination").invoke(response)).isSameAs(pagination);
    }

    private static void assertApiField(Class<?> dtoType, String fieldName) throws Exception {
        ApiField apiField = dtoType.getDeclaredField(fieldName).getAnnotation(ApiField.class);

        assertThat(apiField)
                .as("%s.%s must have @ApiField", dtoType.getSimpleName(), fieldName)
                .isNotNull();
        assertThat(apiField.description()).isNotBlank();
        assertThat(Arrays.asList(apiField.optional()))
                .as("%s.%s should be optional in at least one endpoint", dtoType.getSimpleName(), fieldName)
                .isNotEmpty();
    }
}
