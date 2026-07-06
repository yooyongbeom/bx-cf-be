package com.bwg.channel.backend.common.domain.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CommonDtoContractTests {

    @Test
    void baseAuditRequestDtoDefinesAuditWriters() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseAuditReqDto");

        assertApiField(dtoType, "createdBy");
        assertApiField(dtoType, "updatedBy");
    }

    @Test
    void baseAuditResponseDtoDefinesAuditWritersAndTimestamps() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseAuditResDto");

        assertApiField(dtoType, "createdBy");
        assertApiField(dtoType, "updatedBy");
        assertApiField(dtoType, "createdAt");
        assertApiField(dtoType, "updatedAt");
    }

    @Test
    void baseSearchRequestDtoDefinesCommonSearchFields() throws Exception {
        Class<?> dtoType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseSearchReqDto");

        assertApiField(dtoType, "keyword");
        assertApiField(dtoType, "searchType");
        assertApiField(dtoType, "useYn");
        assertApiField(dtoType, "page");
        assertApiField(dtoType, "size");
        assertApiField(dtoType, "sort");
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
