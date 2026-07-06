package com.bwg.channel.backend.productsvc.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonDtoInheritanceTests {

    @Test
    void productListRequestUsesSearchRequestBaseClass() throws Exception {
        Class<?> searchRequestType = Class.forName("com.bwg.channel.backend.common.domain.dto.BaseSearchReqDto");

        assertThat(searchRequestType.isAssignableFrom(ProductReqDto.class))
                .as("ProductReqDto should extend BaseSearchReqDto")
                .isTrue();
    }
}
