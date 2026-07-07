package com.bwg.channel.backend.productsvc.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCommonDtoInheritanceTests {

    @Test
    void productRequestIsUsedAsApiRequestDataBlock() throws Exception {
        assertThat(ProductReqDto.class.getSuperclass()).isEqualTo(Object.class);
        assertThat(ProductReqDto.class.getDeclaredField("productNm")).isNotNull();
    }
}
